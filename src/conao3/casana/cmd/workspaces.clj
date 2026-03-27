(ns conao3.casana.cmd.workspaces
  (:require
    [conao3.casana.api :as api]
    [conao3.casana.config :as config]
    [conao3.casana.output :as output]))


(def ^:private columns [:gid :name])


(defn list-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))]
    (output/display (:output opts :table) columns (api/get! cfg "/workspaces"))))
