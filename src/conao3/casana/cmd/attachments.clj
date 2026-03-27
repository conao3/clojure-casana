(ns conao3.casana.cmd.attachments
  (:require
   [conao3.casana.api :as api]
   [conao3.casana.config :as config]
   [conao3.casana.output :as output]))

(set! *warn-on-reflection* true)

(def ^:private columns [:gid :name :resource_subtype :view_url])


(defn list-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/tasks/" (:task opts)
                                       "/attachments?opt_fields=gid,name,resource_subtype,view_url")))))


(defn create-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        params {:parent (:task opts)
                :resource_subtype "external"
                :name (or (:name opts) (:url opts))
                :url (:url opts)}]
    (output/display-one (:output opts :table) columns
                        (api/post-form! cfg "/attachments" params))))


(defn delete-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/delete! cfg (str "/attachments/" (:gid opts)))
    (println "Attachment deleted.")))


(defn link-github-pr-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        missing (remove cfg [:ticket :session-auth-token])]
    (when (seq missing)
      (throw (ex-info (str "Missing config: " (clojure.string/join ", " (map name missing))
                           ". Run 'casana configure' and provide the browser cookie string.")
                      {})))
    (let [attachment-gid (+ (* (System/currentTimeMillis) 1000) (rand-int 1000))
          sign-result (api/sign-attach! cfg (:task opts) (:url opts) attachment-gid)
          result (api/github-widget! sign-result)]
      (output/display-one (:output opts :table) [:resource_name :resource_url] result))))
