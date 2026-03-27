(ns conao3.casana.config-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [conao3.casana.config :as config])
  (:import
    (java.nio.file
      Files)
    (java.nio.file.attribute
      FileAttribute)))


(defn- create-tmp-dir
  []
  (str (Files/createTempDirectory "casana-test" (make-array FileAttribute 0))))


(deftest save-and-load-config-test
  (testing "save and load roundtrip"
    (let [tmp (create-tmp-dir)]
      (with-redefs-fn {#'conao3.casana.config/config-path
                       (fn [profile] (str tmp "/" (name profile) ".edn"))}
        (fn []
          (config/save-config :default {:access-token "tok123" :workspace "ws456"})
          (is (= {:access-token "tok123" :workspace "ws456"}
                 (config/load-config :default)))))))
  (testing "different profiles are independent"
    (let [tmp (create-tmp-dir)]
      (with-redefs-fn {#'conao3.casana.config/config-path
                       (fn [profile] (str tmp "/" (name profile) ".edn"))}
        (fn []
          (config/save-config :work {:access-token "work-tok"})
          (config/save-config :personal {:access-token "personal-tok"})
          (is (= "work-tok" (:access-token (config/load-config :work))))
          (is (= "personal-tok" (:access-token (config/load-config :personal)))))))))


(deftest load-missing-config-test
  (testing "returns nil when config file does not exist"
    (with-redefs-fn {#'conao3.casana.config/config-path
                     (fn [_] "/nonexistent/path/config.edn")}
      (fn []
        (is (nil? (config/load-config :default)))))))
