(ns conao3.casana.cmd.configure
  (:require
   [conao3.casana.config :as config]))


(defn run
  [{:keys [opts]}]
  (let [profile (:profile opts :default)
        console (System/console)
        token (if console
                (String. (.readPassword console "Asana Access Token: " (object-array 0)))
                (do (print "Asana Access Token: ")
                    (flush)
                    (read-line)))
        _ (print "Default Workspace GID (optional): ")
        _ (flush)
        workspace (read-line)]
    (config/save-config
     profile
     (cond-> {:access-token token}
       (seq workspace) (assoc :workspace workspace)))
    (println "Configuration saved.")))
