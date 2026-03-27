(ns conao3.casana.cmd.users
  (:require
   [conao3.casana.api :as api]
   [conao3.casana.config :as config]
   [conao3.casana.output :as output]))

(set! *warn-on-reflection* true)

(def ^:private columns [:gid :name :email])


(defn list-cmd
  [{:keys [opts]}]
  (let [cfg (config/load-config (:profile opts :default))
        workspace (or (:workspace opts) (:workspace cfg))]
    (output/display (:output opts :table) columns
                    (api/get! cfg (str "/workspaces/" workspace "/users")))))
