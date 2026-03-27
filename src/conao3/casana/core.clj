(ns conao3.casana.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [conao3.casana.cmd.comments :as comments]
   [conao3.casana.cmd.configure :as configure]
   [conao3.casana.cmd.projects :as projects]
   [conao3.casana.cmd.sections :as sections]
   [conao3.casana.cmd.tasks :as tasks]
   [conao3.casana.cmd.workspaces :as workspaces]))


(def ^:private global-spec
  {:output    {:desc "Output format (json|table|text)" :default :table   :coerce :keyword}
   :profile   {:desc "Configuration profile"           :default :default :coerce :keyword}
   :workspace {:desc "Workspace GID override"}})


(defn- print-help
  [_]
  (println "Usage: casana <resource> <action> [options]")
  (println)
  (println "Resources:")
  (println "  configure")
  (println "  workspaces list")
  (println "  projects list [--workspace <gid>]")
  (println "  projects get --gid <gid>")
  (println "  sections list --project <gid>")
  (println "  sections get --gid <gid>")
  (println "  tasks list [--project <gid>] [--section <gid>] [--assignee <gid>]")
  (println "  tasks get --gid <gid>")
  (println "  tasks create --name <name> [--project <gid>] [--section <gid>] [--due <date>]")
  (println "  tasks update --gid <gid> [--name <name>] [--due <date>]")
  (println "  tasks complete --gid <gid>")
  (println "  tasks move --gid <gid> --section <gid>")
  (println "  tasks delete --gid <gid>")
  (println "  comments list --task <gid>")
  (println "  comments create --task <gid> --text <text>")
  (println)
  (println "Global options:")
  (println "  --output json|table|text  Output format (default: table)")
  (println "  --profile <name>          Configuration profile (default: default)")
  (println "  --workspace <gid>         Workspace GID override"))


(def ^:private dispatch-table
  [{:cmds ["configure"]          :fn configure/run}
   {:cmds ["workspaces" "list"]  :fn workspaces/list-cmd}
   {:cmds ["projects" "list"]    :fn projects/list-cmd}
   {:cmds ["projects" "get"]     :fn projects/get-cmd}
   {:cmds ["sections" "list"]    :fn sections/list-cmd}
   {:cmds ["sections" "get"]     :fn sections/get-cmd}
   {:cmds ["tasks" "list"]       :fn tasks/list-cmd}
   {:cmds ["tasks" "get"]        :fn tasks/get-cmd}
   {:cmds ["tasks" "create"]     :fn tasks/create-cmd}
   {:cmds ["tasks" "update"]     :fn tasks/update-cmd}
   {:cmds ["tasks" "complete"]   :fn tasks/complete-cmd}
   {:cmds ["tasks" "move"]       :fn tasks/move-cmd}
   {:cmds ["tasks" "delete"]     :fn tasks/delete-cmd}
   {:cmds ["comments" "list"]    :fn comments/list-cmd}
   {:cmds ["comments" "create"]  :fn comments/create-cmd}
   {:cmds []                     :fn print-help}])


(defn -main
  [& args]
  (cli/dispatch dispatch-table args {:spec global-spec}))
