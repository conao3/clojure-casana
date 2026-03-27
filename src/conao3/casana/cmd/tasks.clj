(ns conao3.casana.cmd.tasks
  (:require [clojure.string :as str]
            [conao3.casana.api :as api]
            [conao3.casana.config :as config]
            [conao3.casana.output :as output]))

(def ^:private columns [:gid :name :due_on :assignee :completed])

(defn- build-query [params]
  (->> params
       (map (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))

(defn list-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        params (cond-> {}
                 (:project opts) (assoc :project (:project opts))
                 (:section opts) (assoc :section (:section opts))
                 (:assignee opts) (assoc :assignee (:assignee opts)))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/tasks?" (build-query params))))))

(defn get-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    [(api/get! cfg (str "/tasks/" (:gid opts)))])))

(defn create-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        body (cond-> {:name (:name opts)}
               (:project opts) (assoc :projects [(:project opts)])
               (:section opts) (assoc :memberships [{:project (:project opts)
                                                     :section (:section opts)}])
               (:due opts) (assoc :due_on (:due opts))
               (:assignee opts) (assoc :assignee (:assignee opts)))]
    (output/display (:output opts :table) columns
                    [(api/post! cfg "/tasks" body)])))

(defn update-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        body (cond-> {}
               (:name opts) (assoc :name (:name opts))
               (:due opts) (assoc :due_on (:due opts))
               (:assignee opts) (assoc :assignee (:assignee opts)))]
    (output/display (:output opts :table) columns
                    [(api/put! cfg (str "/tasks/" (:gid opts)) body)])))

(defn complete-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    [(api/put! cfg (str "/tasks/" (:gid opts)) {:completed true})])))

(defn move-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/post! cfg (str "/sections/" (:section opts) "/addTask") {:task (:gid opts)})
    (println "Task moved successfully.")))

(defn delete-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/delete! cfg (str "/tasks/" (:gid opts)))
    (println "Task deleted.")))
