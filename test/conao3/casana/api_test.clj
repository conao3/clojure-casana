(ns conao3.casana.api-test
  (:require
   [clj-http.client :as http]
   [clojure.string :as str]
   [clojure.test :as t]
   [conao3.casana.api :as api]))


(def ^:private test-config {:access-token "test-token"})


(t/deftest get!-test
  (t/testing "calls GET with correct URL and auth header"
    (let [captured (atom nil)]
      (with-redefs [http/get (fn [url opts]
                               (reset! captured {:url url :opts opts})
                               {:body {:data []}})]
        (api/get! test-config "/workspaces")
        (t/is (= "https://app.asana.com/api/1.0/workspaces" (:url @captured)))
        (t/is (= "Bearer test-token"
                 (get-in @captured [:opts :headers "Authorization"])))))))


(t/deftest post!-test
  (t/testing "calls POST with correct URL and JSON body"
    (let [captured (atom nil)]
      (with-redefs [http/post (fn [url opts]
                                (reset! captured {:url url :opts opts})
                                {:body {:data {:gid "1"}}})]
        (api/post! test-config "/tasks" {:name "new task"})
        (t/is (= "https://app.asana.com/api/1.0/tasks" (:url @captured)))
        (t/is (str/includes? (get-in @captured [:opts :body]) "new task"))
        (t/is (= "Bearer test-token"
                 (get-in @captured [:opts :headers "Authorization"]))))))
  (t/testing "wraps body in :data envelope"
    (let [captured (atom nil)]
      (with-redefs [http/post (fn [_ opts]
                                (reset! captured opts)
                                {:body {:data {}}})]
        (api/post! test-config "/tasks" {:name "task"})
        (t/is (str/includes? (:body @captured) "\"data\""))))))


(t/deftest put!-test
  (t/testing "calls PUT with correct URL"
    (let [captured (atom nil)]
      (with-redefs [http/put (fn [url opts]
                               (reset! captured {:url url :opts opts})
                               {:body {:data {:gid "1" :completed true}}})]
        (api/put! test-config "/tasks/1" {:completed true})
        (t/is (= "https://app.asana.com/api/1.0/tasks/1" (:url @captured)))))))


(t/deftest delete!-test
  (t/testing "calls DELETE with correct URL"
    (let [captured (atom nil)]
      (with-redefs [http/delete (fn [url opts]
                                  (reset! captured {:url url :opts opts})
                                  {:body {:data {}}})]
        (api/delete! test-config "/tasks/1")
        (t/is (= "https://app.asana.com/api/1.0/tasks/1" (:url @captured)))))))
