(ns conao3.casana.cmd.projects
  (:require
   [conao3.casana.api :as api]
   [conao3.casana.config :as config]
   [conao3.casana.output :as output]))


(def ^:private columns [:gid :name :color :archived])


(defn list-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        workspace (or (:workspace opts) (:workspace cfg))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/projects?workspace=" workspace)))))


(defn get-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display-one (:output opts :table) columns
                        (api/get! cfg (str "/projects/" (:gid opts))))))
