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


(def ^:private get-opt-fields
  (str "gid,name,due_on,assignee,assignee.name,completed"
       ",custom_fields,custom_fields.gid,custom_fields.name,custom_fields.display_value"))


(defn get-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        task (api/get! cfg (str "/tasks/" (:gid opts) "?opt_fields=" get-opt-fields))
        attachments (api/get! cfg (str "/tasks/" (:gid opts)
                                       "/attachments?opt_fields=gid,name,resource_subtype,view_url"))
        task+att (assoc task :attachments attachments)]
    (if (= :json (:output opts :table))
      (output/display :json (conj columns :attachments) [task+att])
      (do
        (output/display :table columns [task])
        (when (seq (:custom_fields task))
          (println)
          (println "Custom fields:")
          (doseq [{field-name :name field-value :display_value} (:custom_fields task)]
            (println (str "  " field-name ": " (or field-value "-")))))
        (when (seq attachments)
          (println)
          (println "Attachments:")
          (doseq [{att-name :name att-url :view_url} attachments]
            (println (str "  " att-name ": " (or att-url "-")))))))))


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


(defn- parse-field-opt
  [s]
  (let [idx (str/index-of s "=")]
    {:name (subs s 0 idx)
     :value (subs s (inc idx))}))


(defn- resolve-custom-field
  [cfg task-gid field-name]
  (let [task (api/get! cfg (str "/tasks/" task-gid
                                "?opt_fields=custom_fields,custom_fields.gid,custom_fields.name"))]
    (->> (:custom_fields task)
         (filter #(= (:name %) field-name))
         first
         :gid)))


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
        custom-fields (when (:field opts)
                        (let [{field-name :name field-value :value} (parse-field-opt (:field opts))
                              field-gid (resolve-custom-field cfg (:gid opts) field-name)]
                          (when field-gid
                            {(keyword field-gid) (when (seq field-value) field-value)})))
        body (cond-> {}
               (:name opts) (assoc :name (:name opts))
               (:notes opts) (assoc :notes (:notes opts))
               (:due opts) (assoc :due_on (:due opts))
               (:assignee opts) (assoc :assignee (:assignee opts))
               custom-fields (assoc :custom_fields custom-fields))
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
