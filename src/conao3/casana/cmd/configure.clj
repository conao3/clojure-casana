(ns conao3.casana.cmd.configure
  (:require
   [clojure.string :as str]
   [conao3.casana.config :as config]))

(set! *warn-on-reflection* true)


(defn- parse-cookie
  [cookie-str]
  (->> (str/split cookie-str #";\s*")
       (map #(let [[k v] (str/split % #"=" 2)]
               [(str/trim k) v]))
       (into {})))


(defn run
  [{:keys [opts]}]
  (let [profile (:profile opts :default)
        existing (or (config/load-config profile) {})
        console (System/console)
        token (if console
                (String. (.readPassword console "Asana Access Token: " (object-array 0)))
                (do (print "Asana Access Token: ")
                    (flush)
                    (read-line)))
        _ (print "Default Workspace GID (optional): ")
        _ (flush)
        workspace (read-line)
        _ (print "Browser Cookie String (optional, for GitHub PR widgets): ")
        _ (flush)
        cookie-str (read-line)
        cookie (when (seq cookie-str) (parse-cookie cookie-str))
        ticket (get cookie "ticket")
        session-auth-token (get cookie "auth_token")]
    (config/save-config
     profile
     (cond-> existing
       (seq token) (assoc :access-token token)
       (seq workspace) (assoc :workspace workspace)
       ticket (assoc :ticket ticket)
       session-auth-token (assoc :session-auth-token session-auth-token)))
    (println "Configuration saved.")))
