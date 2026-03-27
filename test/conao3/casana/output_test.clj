(ns conao3.casana.output-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest is testing]]
    [conao3.casana.output :as output]))


(def ^:private cell-str #'output/cell-str)


(deftest cell-str-test
  (testing "nil returns dash"
    (is (= "-" (cell-str nil))))
  (testing "string value"
    (is (= "hello" (cell-str "hello"))))
  (testing "number value"
    (is (= "42" (cell-str 42))))
  (testing "boolean value"
    (is (= "true" (cell-str true))))
  (testing "map with :name"
    (is (= "Alice" (cell-str {:gid "123" :name "Alice"}))))
  (testing "map without :name"
    (is (str/includes? (cell-str {:gid "123"}) "123"))))


(deftest display-table-test
  (testing "header row is uppercased"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (is (str/includes? out "GID"))
      (is (str/includes? out "NAME"))))
  (testing "data rows are rendered"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                [{:gid "1" :name "Task A"}
                                 {:gid "2" :name "Task B"}]))]
      (is (str/includes? out "Task A"))
      (is (str/includes? out "Task B"))))
  (testing "nil values shown as dash"
    (let [out (with-out-str
                (output/display :table [:gid :due_on]
                                [{:gid "1"}]))]
      (is (str/includes? out "-"))))
  (testing "single map is treated as one row"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                {:gid "1" :name "Task A"}))]
      (is (str/includes? out "Task A")))))


(deftest display-text-test
  (testing "values are tab-separated"
    (let [out (with-out-str
                (output/display :text [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (is (str/includes? out "1\tTask A"))))
  (testing "multiple rows each on own line"
    (let [lines (-> (with-out-str
                      (output/display :text [:gid :name]
                                      [{:gid "1" :name "A"}
                                       {:gid "2" :name "B"}]))
                    str/split-lines)]
      (is (= 2 (count lines))))))


(deftest display-json-test
  (testing "output is valid JSON containing the data"
    (let [out (with-out-str
                (output/display :json [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (is (str/includes? out "\"gid\""))
      (is (str/includes? out "Task A")))))
