(ns conao3.casana.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [cheshire.core :as json]
   [clojure.string :as str]
   [conao3.casana.cmd.attachments :as attachments]
   [conao3.casana.cmd.comments :as comments]
   [conao3.casana.cmd.configure :as configure]
   [conao3.casana.cmd.projects :as projects]
   [conao3.casana.cmd.sections :as sections]
   [conao3.casana.cmd.tasks :as tasks]
   [conao3.casana.cmd.users :as users]
   [conao3.casana.cmd.workspaces :as workspaces]))

(set! *warn-on-reflection* true)


(def ^:private global-spec
  {:help {:desc "Show this help" :coerce :boolean}
   :output {:desc "Output format (json|table|text)" :default :table :coerce :keyword}
   :profile {:desc "Configuration profile" :default :default :coerce :keyword}
   :workspace {:desc "Workspace GID override"}})


(defn- format-spec
  [spec]
  (->> spec
       (sort-by (comp name key))
       (map (fn [[k v]]
              (let [default (when-let [d (:default v)]
                              (str " [default: " (if (keyword? d) (name d) d) "]"))]
                (format "  %-20s %s%s" (str "--" (name k)) (:desc v "") (or default "")))))
       (str/join "\n")))


(defn- print-cmd-help
  [usage cmd-spec]
  (println (str "Usage: casana " usage))
  (when (seq cmd-spec)
    (println)
    (println "Options:")
    (println (format-spec cmd-spec)))
  (println)
  (println "Global options:")
  (println (format-spec global-spec)))


(defn- with-help
  [usage cmd-spec f]
  (fn [{:keys [opts] :as m}]
    (if (:help opts)
      (print-cmd-help usage cmd-spec)
      (let [allowed (into #{:help} (concat (keys cmd-spec) (keys global-spec)))
            unknown (remove allowed (keys opts))]
        (if (seq unknown)
          (do
            (binding [*out* *err*]
              (println (str "Unknown option(s): "
                            (str/join ", " (map #(str "--" (name %)) unknown))))
              (println))
            (print-cmd-help usage cmd-spec)
            (System/exit 1))
          (f m))))))


(defn- print-global-help
  [_]
  (println "Usage: casana <resource> <action> [options]")
  (println)
  (println "Resources:")
  (println "  configure")
  (println "  workspaces list")
  (println "  users list")
  (println "  projects list|get")
  (println "  sections list|get")
  (println "  tasks list|get|create|update|complete|move|delete")
  (println "  comments list|create")
  (println "  attachments list|create|delete")
  (println)
  (println "Global options:")
  (println (format-spec global-spec)))


(def ^:private dispatch-table
  [{:cmds ["configure"]
    :fn (with-help "configure" {} configure/run)}
   {:cmds ["workspaces" "list"]
    :fn (with-help "workspaces list" {} workspaces/list-cmd)}
   {:cmds ["users" "list"]
    :fn (with-help "users list"
          {:workspace {:desc "Workspace GID"}}
          users/list-cmd)}
   {:cmds ["projects" "list"]
    :fn (with-help "projects list"
          {:workspace {:desc "Workspace GID"}}
          projects/list-cmd)}
   {:cmds ["projects" "get"]
    :fn (with-help "projects get --gid <gid>"
          {:gid {:desc "Project GID"}}
          projects/get-cmd)}
   {:cmds ["sections" "list"]
    :fn (with-help "sections list --project <gid>"
          {:project {:desc "Project GID"}}
          sections/list-cmd)}
   {:cmds ["sections" "get"]
    :fn (with-help "sections get --gid <gid>"
          {:gid {:desc "Section GID"}}
          sections/get-cmd)}
   {:cmds ["tasks" "list"]
    :fn (with-help "tasks list [--project <gid>] [--section <gid>] [--assignee <gid>]"
          {:assignee {:desc "Assignee GID or 'me'"}
           :project {:desc "Project GID (mutually exclusive with --section)"}
           :section {:desc "Section GID (mutually exclusive with --project)"}}
          tasks/list-cmd)}
   {:cmds ["tasks" "get"]
    :fn (with-help "tasks get --gid <gid>"
          {:gid {:desc "Task GID"}}
          tasks/get-cmd)}
   {:cmds ["tasks" "create"]
    :fn (with-help "tasks create --name <name> [options]"
          {:assignee {:desc "Assignee GID or 'me'"}
           :due {:desc "Due date (YYYY-MM-DD)"}
           :name {:desc "Task name"}
           :notes {:desc "Task notes (body text)"}
           :project {:desc "Project GID"}
           :section {:desc "Section GID"}}
          tasks/create-cmd)}
   {:cmds ["tasks" "update"]
    :fn (with-help "tasks update --gid <gid> [options]"
          {:assignee {:desc "New assignee GID or 'me'"}
           :dependencies {:desc "Dependency task GIDs (comma-separated; empty string clears all)"}
           :due {:desc "New due date (YYYY-MM-DD)"}
           :field {:desc "Custom field value as name=value (empty value clears the field)"}
           :gid {:desc "Task GID"}
           :name {:desc "New task name"}
           :notes {:desc "New task notes (body text)"}}
          tasks/update-cmd)}
   {:cmds ["tasks" "complete"]
    :fn (with-help "tasks complete --gid <gid>"
          {:gid {:desc "Task GID"}}
          tasks/complete-cmd)}
   {:cmds ["tasks" "move"]
    :fn (with-help "tasks move --gid <gid> --section <gid>"
          {:gid {:desc "Task GID"}
           :section {:desc "Destination section GID"}}
          tasks/move-cmd)}
   {:cmds ["tasks" "delete"]
    :fn (with-help "tasks delete --gid <gid>"
          {:gid {:desc "Task GID"}}
          tasks/delete-cmd)}
   {:cmds ["comments" "list"]
    :fn (with-help "comments list --task <gid>"
          {:task {:desc "Task GID"}}
          comments/list-cmd)}
   {:cmds ["comments" "create"]
    :fn (with-help "comments create --task <gid> --text <text>"
          {:task {:desc "Task GID"}
           :text {:desc "Comment text"}}
          comments/create-cmd)}
   {:cmds ["attachments" "list"]
    :fn (with-help "attachments list --task <gid>"
          {:task {:desc "Task GID"}}
          attachments/list-cmd)}
   {:cmds ["attachments" "create"]
    :fn (with-help "attachments create --task <gid> --url <url> [--name <name>]"
          {:name {:desc "Display name"}
           :task {:desc "Task GID"}
           :url {:desc "URL to link"}}
          attachments/create-cmd)}
   {:cmds ["attachments" "delete"]
    :fn (with-help "attachments delete --gid <gid>"
          {:gid {:desc "Attachment GID"}}
          attachments/delete-cmd)}
   {:cmds []
    :fn print-global-help}])


(defn -main
  [& args]
  (try
    (cli/dispatch dispatch-table args {:spec global-spec})
    (catch clojure.lang.ExceptionInfo e
      (let [body (some-> e ex-data :body)
            msgs (when body
                   (->> (get (json/parse-string body true) :errors)
                        (map :message)))]
        (binding [*out* *err*]
          (if (seq msgs)
            (doseq [msg msgs] (println (str "Error: " msg)))
            (println (str "Error: " (ex-message e)))))
        (System/exit 1)))))
