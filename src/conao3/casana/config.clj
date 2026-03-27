(ns conao3.casana.config
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(defn- config-path
  [profile]
  (str (System/getProperty "user.home")
       "/.config/casana/"
       (name profile)
       ".edn"))


(defn load-config
  [profile]
  (let [f (io/file (config-path profile))]
    (when (.exists f)
      (-> f slurp edn/read-string))))


(defn save-config
  [profile config]
  (let [f (io/file (config-path profile))]
    (io/make-parents f)
    (spit f (pr-str config))))
