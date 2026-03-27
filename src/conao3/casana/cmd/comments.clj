(ns conao3.casana.cmd.comments
  (:require [conao3.casana.api :as api]
            [conao3.casana.config :as config]
            [conao3.casana.output :as output]))

(def ^:private columns [:gid :created_at :created_by :text])

(defn list-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/tasks/" (:task opts) "/stories?type=comment")))))

(defn create-cmd [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    [(api/post! cfg (str "/tasks/" (:task opts) "/stories")
                                {:text (:text opts)})])))
