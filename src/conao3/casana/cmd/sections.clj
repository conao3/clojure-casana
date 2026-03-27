(ns conao3.casana.cmd.sections
  (:require
   [conao3.casana.api :as api]
   [conao3.casana.config :as config]
   [conao3.casana.output :as output]))


(def ^:private columns [:gid :name])


(defn list-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/projects/" (:project opts) "/sections")))))


(defn get-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display-one (:output opts :table) columns
                        (api/get! cfg (str "/sections/" (:gid opts))))))
