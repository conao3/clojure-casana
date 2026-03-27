(ns conao3.casana.api-test
  (:require
    [clj-http.client :as http]
    [clojure.string :as str]
    [clojure.test :refer [deftest is testing]]
    [conao3.casana.api :as api]))


(def ^:private test-config {:access-token "test-token"})


(deftest get!-test
  (testing "calls GET with correct URL and auth header"
    (let [captured (atom nil)]
      (with-redefs [http/get (fn [url opts]
                               (reset! captured {:url url :opts opts})
                               {:body {:data []}})]
        (api/get! test-config "/workspaces")
        (is (= "https://app.asana.com/api/1.0/workspaces" (:url @captured)))
        (is (= "Bearer test-token"
               (get-in @captured [:opts :headers "Authorization"])))))))


(deftest post!-test
  (testing "calls POST with correct URL and JSON body"
    (let [captured (atom nil)]
      (with-redefs [http/post (fn [url opts]
                                (reset! captured {:url url :opts opts})
                                {:body {:data {:gid "1"}}})]
        (api/post! test-config "/tasks" {:name "new task"})
        (is (= "https://app.asana.com/api/1.0/tasks" (:url @captured)))
        (is (str/includes? (get-in @captured [:opts :body]) "new task"))
        (is (= "Bearer test-token"
               (get-in @captured [:opts :headers "Authorization"]))))))
  (testing "wraps body in :data envelope"
    (let [captured (atom nil)]
      (with-redefs [http/post (fn [_ opts]
                                (reset! captured opts)
                                {:body {:data {}}})]
        (api/post! test-config "/tasks" {:name "task"})
        (is (str/includes? (:body @captured) "\"data\""))))))


(deftest put!-test
  (testing "calls PUT with correct URL"
    (let [captured (atom nil)]
      (with-redefs [http/put (fn [url opts]
                               (reset! captured {:url url :opts opts})
                               {:body {:data {:gid "1" :completed true}}})]
        (api/put! test-config "/tasks/1" {:completed true})
        (is (= "https://app.asana.com/api/1.0/tasks/1" (:url @captured)))))))


(deftest delete!-test
  (testing "calls DELETE with correct URL"
    (let [captured (atom nil)]
      (with-redefs [http/delete (fn [url opts]
                                  (reset! captured {:url url :opts opts})
                                  {:body {:data {}}})]
        (api/delete! test-config "/tasks/1")
        (is (= "https://app.asana.com/api/1.0/tasks/1" (:url @captured)))))))
