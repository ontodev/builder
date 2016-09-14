(ns ontodev.builder.tasks.queue-test
  (:require [clojure.test :refer :all]
            [ontodev.builder.tasks.queue :refer :all]))

(def test-task {:var (fn [] "test task") :name "Best Task"})
(def long-task {:var (fn [] (Thread/sleep 5000) "long task") :name "Long Task"})

(deftest test-execute-task
  (testing "Attempt to add a malformed task and fail"
    (is (thrown-with-msg?
         RuntimeException
         #"Task is malformed"
         (execute-task "")))
    (is (thrown-with-msg?
         RuntimeException
         #"Task name not provided"
         (execute-task
          {:var  ""
           :name nil}))))

  (testing "Accept task"
    (let [execution (execute-task test-task)
          id        (:id execution)]
      (is (= execution
             (get-execution id)
             {:id id
              :name "Best Task"
              :status :done
              :result "test task"})))))

(deftest test-cancel-execution
  (testing "Execute a task then cancel execution"
    (is (nil? (get-current-execution)))
    (let [execution (execute-task long-task)
          id (:id execution)]
      (is (not (nil? (get-current-execution))))
      (is (= :pending (:status (get-execution id))))
      (cancel-execution id)
      (is (nil? (get-current-execution)))
      (is (= (get-execution id)
             {:id id
              :name "Long Task"
              :status :cancelled
              :result nil})))))
