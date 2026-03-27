(ns conao3.casana.cmd.tasks
  (:require
   [clojure.string :as str]
   [conao3.casana.api :as api]
   [conao3.casana.config :as config]
   [conao3.casana.output :as output]))

(set! *warn-on-reflection* true)

(defn- exit!
  [code]
  (System/exit code))


(def ^:private columns [:gid :name :due_on :assignee :completed])


(defn- build-query
  [params]
  (->> params
       (map (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))


(defn list-cmd
  [{:keys [opts]}]
  (if (and (:project opts) (:section opts))
    (do
      (binding [*out* *err*]
        (println "Error: --project and --section are mutually exclusive"))
      (exit! 1))
    (let [cfg (config/load-config (:profile opts :default))
          params (cond-> {}
                   (:project opts) (assoc :project (:project opts))
                   (:section opts) (assoc :section (:section opts))
                   (:assignee opts) (assoc :assignee (:assignee opts)))]
      (output/display (:output opts :table) columns
                      (api/get! cfg (str "/tasks?" (build-query params)))))))


(defn get-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    [(api/get! cfg (str "/tasks/" (:gid opts)))])))


(defn create-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        body (cond-> {:name (:name opts)}
               (:workspace opts) (assoc :workspace (:workspace opts))
               (and (not (:workspace opts)) (:workspace cfg)) (assoc :workspace (:workspace cfg))
               (:project opts) (assoc :projects [(:project opts)])
               (and (:project opts) (:section opts)) (assoc :memberships [{:project (:project opts)
                                                                           :section (:section opts)}])
               (:notes opts) (assoc :notes (:notes opts))
               (:due opts) (assoc :due_on (:due opts))
               (:assignee opts) (assoc :assignee (:assignee opts)))
        task (api/post! cfg "/tasks" body)]
    (when (and (:section opts) (not (:project opts)))
      (api/post! cfg (str "/sections/" (:section opts) "/addTask") {:task (:gid task)}))
    (output/display (:output opts :table) columns [task])))


(defn- update-dependencies!
  [cfg gid new-deps]
  (let [current (->> (api/get! cfg (str "/tasks/" gid "/dependencies"))
                     (mapv :gid))]
    (when (seq current)
      (api/post! cfg (str "/tasks/" gid "/removeDependencies") {:dependencies current}))
    (when (seq new-deps)
      (api/post! cfg (str "/tasks/" gid "/addDependencies") {:dependencies new-deps}))))


(defn update-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        body (cond-> {}
               (:name opts) (assoc :name (:name opts))
               (:notes opts) (assoc :notes (:notes opts))
               (:due opts) (assoc :due_on (:due opts))
               (:assignee opts) (assoc :assignee (:assignee opts)))
        task (api/put! cfg (str "/tasks/" (:gid opts)) body)]
    (when (some? (:dependencies opts))
      (update-dependencies! cfg (:gid opts)
                            (when (seq (:dependencies opts))
                              (str/split (:dependencies opts) #","))))
    (output/display (:output opts :table) columns [task])))


(defn complete-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    [(api/put! cfg (str "/tasks/" (:gid opts)) {:completed true})])))


(defn move-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/post! cfg (str "/sections/" (:section opts) "/addTask") {:task (:gid opts)})
    (println "Task moved successfully.")))


(defn delete-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/delete! cfg (str "/tasks/" (:gid opts)))
    (println "Task deleted.")))
