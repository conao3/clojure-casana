(ns conao3.casana.output-test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [conao3.casana.output :as output]))


(def ^:private cell-str #'output/cell-str)


(t/deftest cell-str-test
  (t/testing "nil returns dash"
    (t/is (= "-" (cell-str nil))))
  (t/testing "string value"
    (t/is (= "hello" (cell-str "hello"))))
  (t/testing "number value"
    (t/is (= "42" (cell-str 42))))
  (t/testing "boolean value"
    (t/is (= "true" (cell-str true))))
  (t/testing "map with :name"
    (t/is (= "Alice" (cell-str {:gid "123" :name "Alice"}))))
  (t/testing "map without :name"
    (t/is (str/includes? (cell-str {:gid "123"}) "123"))))


(t/deftest display-table-test
  (t/testing "header row is uppercased"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (t/is (str/includes? out "GID"))
      (t/is (str/includes? out "NAME"))))
  (t/testing "data rows are rendered"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                [{:gid "1" :name "Task A"}
                                 {:gid "2" :name "Task B"}]))]
      (t/is (str/includes? out "Task A"))
      (t/is (str/includes? out "Task B"))))
  (t/testing "nil values shown as dash"
    (let [out (with-out-str
                (output/display :table [:gid :due_on]
                                [{:gid "1"}]))]
      (t/is (str/includes? out "-"))))
  (t/testing "single map is treated as one row"
    (let [out (with-out-str
                (output/display :table [:gid :name]
                                {:gid "1" :name "Task A"}))]
      (t/is (str/includes? out "Task A")))))


(t/deftest display-text-test
  (t/testing "values are tab-separated"
    (let [out (with-out-str
                (output/display :text [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (t/is (str/includes? out "1\tTask A"))))
  (t/testing "multiple rows each on own line"
    (let [lines (-> (with-out-str
                      (output/display :text [:gid :name]
                                      [{:gid "1" :name "A"}
                                       {:gid "2" :name "B"}]))
                    str/split-lines)]
      (t/is (= 2 (count lines))))))


(t/deftest display-json-test
  (t/testing "output is valid JSON containing the data"
    (let [out (with-out-str
                (output/display :json [:gid :name]
                                [{:gid "1" :name "Task A"}]))]
      (t/is (str/includes? out "\"gid\""))
      (t/is (str/includes? out "Task A")))))
