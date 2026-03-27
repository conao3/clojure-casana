(ns conao3.casana.cmd.tasks-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest is testing]]
    [conao3.casana.api :as api]
    [conao3.casana.cmd.tasks :as tasks]
    [conao3.casana.config :as config]))


(def ^:private test-cfg {:access-token "tok"})


(deftest list-cmd-test
  (testing "calls /tasks without filters when no options given"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/get! (fn [_ path] (reset! captured path) [])]
        (with-out-str (tasks/list-cmd {:opts {:profile :default :output :text}}))
        (is (= "/tasks?" @captured)))))
  (testing "includes project in query when --project is given"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/get! (fn [_ path] (reset! captured path) [])]
        (with-out-str (tasks/list-cmd {:opts {:profile :default :output :text :project "123"}}))
        (is (str/includes? @captured "project=123")))))
  (testing "includes section in query when --section is given"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/get! (fn [_ path] (reset! captured path) [])]
        (with-out-str (tasks/list-cmd {:opts {:profile :default :output :text :section "456"}}))
        (is (str/includes? @captured "section=456"))))))


(deftest complete-cmd-test
  (testing "sends completed=true to the task endpoint"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/put! (fn [_ path body]
                               (reset! captured {:path path :body body})
                               {:gid "1" :completed true})]
        (with-out-str (tasks/complete-cmd {:opts {:profile :default :output :text :gid "1"}}))
        (is (= "/tasks/1" (:path @captured)))
        (is (true? (:completed (:body @captured))))))))


(deftest move-cmd-test
  (testing "calls addTask on the section endpoint"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/post! (fn [_ path body]
                                (reset! captured {:path path :body body})
                                {})]
        (with-out-str (tasks/move-cmd {:opts {:profile :default :gid "987" :section "456"}}))
        (is (= "/sections/456/addTask" (:path @captured)))
        (is (= {:task "987"} (:body @captured)))))))


(deftest delete-cmd-test
  (testing "calls DELETE on the task endpoint"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/delete! (fn [_ path]
                                  (reset! captured path)
                                  {})]
        (with-out-str (tasks/delete-cmd {:opts {:profile :default :gid "1"}}))
        (is (= "/tasks/1" @captured))))))


(deftest create-cmd-test
  (testing "sends name in body"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/post! (fn [_ path body]
                                (reset! captured {:path path :body body})
                                {:gid "1" :name "New Task"})]
        (with-out-str (tasks/create-cmd {:opts {:profile :default :output :text :name "New Task"}}))
        (is (= "/tasks" (:path @captured)))
        (is (= "New Task" (get-in @captured [:body :name]))))))
  (testing "includes project when --project is given"
    (let [captured (atom nil)]
      (with-redefs [config/load-config (fn [_] test-cfg)
                    api/post! (fn [_ _ body]
                                (reset! captured body)
                                {:gid "1" :name "Task"})]
        (with-out-str (tasks/create-cmd {:opts {:profile :default :output :text
                                                :name "Task" :project "123"}}))
        (is (= ["123"] (:projects @captured)))))))
