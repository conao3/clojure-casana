(ns conao3.casana.config-test
  (:require
   [clojure.test :as t]
   [conao3.casana.config :as config])
  (:import
   (java.nio.file
    Files)
   (java.nio.file.attribute
    FileAttribute)))

(set! *warn-on-reflection* true)

(defn- create-tmp-dir
  []
  (str (Files/createTempDirectory "casana-test" (make-array FileAttribute 0))))


(t/deftest save-and-load-config-test
  (t/testing "save and load roundtrip"
    (let [tmp (create-tmp-dir)]
      (with-redefs-fn {#'config/config-path
                       (fn [profile] (str tmp "/" (name profile) ".edn"))}
        (fn []
          (config/save-config :default {:access-token "tok123" :workspace "ws456"})
          (t/is (= {:access-token "tok123" :workspace "ws456"}
                   (config/load-config :default)))))))
  (t/testing "different profiles are independent"
    (let [tmp (create-tmp-dir)]
      (with-redefs-fn {#'config/config-path
                       (fn [profile] (str tmp "/" (name profile) ".edn"))}
        (fn []
          (config/save-config :work {:access-token "work-tok"})
          (config/save-config :personal {:access-token "personal-tok"})
          (t/is (= "work-tok" (:access-token (config/load-config :work))))
          (t/is (= "personal-tok" (:access-token (config/load-config :personal)))))))))


(t/deftest load-missing-config-test
  (t/testing "returns nil when config file does not exist"
    (with-redefs-fn {#'config/config-path
                     (fn [_] "/nonexistent/path/config.edn")}
      (fn []
        (t/is (nil? (config/load-config :default)))))))
