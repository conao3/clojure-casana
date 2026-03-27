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
        params (cond-> {:parent (:task opts)
                        :resource_subtype "external"
                        :url (:url opts)}
                 (:name opts) (assoc :name (:name opts)))]
    (output/display-one (:output opts :table) columns
                        (api/post-form! cfg "/attachments" params))))


(defn delete-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (api/delete! cfg (str "/attachments/" (:gid opts)))
    (println "Attachment deleted.")))
