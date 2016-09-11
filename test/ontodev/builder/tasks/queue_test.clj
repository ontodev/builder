(ns ontodev.builder.tasks.queue-test
  (:require [clojure.test :refer :all]
            [ontodev.builder.tasks.queue :refer :all]))

(defn clear-ran-tasks-fixture
  [f]
  (reset! ran-tasks {})
  (f))

(use-fixtures :each clear-ran-tasks-fixture)

(def test-task-result "Result")

(def test-task {:var (fn [] test-task-result) :name "Best Task"})
(def long-task {:var (fn [] (Thread/sleep 5000)) :name "Long Task"})

(defn first-tasks-key
  []
  (-> @ran-tasks
      first
      key))

(deftest test-add-task
  (testing "Add a task and see it in the ran-tasks"
    (add-task test-task)
    (is (not-empty @ran-tasks)))

  (testing "Attempt to add a malformed task and fail"
    (is (thrown-with-msg? RuntimeException
                          #"Malformed task attempted to be added"
                          (add-task "")))
    (is (thrown-with-msg? RuntimeException
                          #"Malformed task attempted to be added"
                          (add-task {:var  ""
                                     :name nil}))))

  (testing "Accept ifn as var params"
    (is (add-task {:var #'first-tasks-key :name "asdf"})))

  (testing "Accept symbol as task name"
    (is (add-task {:var (fn [] "asdf") :name 'test-task}))))

(deftest test-drop-task
  (testing "Drop the first task given its id"
    (add-task test-task)
    (let [k (first-tasks-key)]
      (drop-task k)
      (is (nil? (get @ran-tasks k))))))

(deftest test-get-task-future
  (testing "Ensures a future is returned for a task"
    (add-task test-task)
    (is (future? (get-task-future (first-tasks-key)))))

  (testing "Get a nonexistent task"
    (is (nil? (get-task-future 123)))))

(deftest test-get-task
  (testing "Get a completed task"
    (add-task test-task)
    (let [k (first-tasks-key)]
      (= (get-task k) test-task-result)))

  (testing "Get a cancelled task"
    (add-task long-task)
    (let [k (first-tasks-key)]
      (cancel-task k)
      (= (get-task k) "Task cancelled!")))

  (testing "Get an unfinished task"
    (add-task long-task)
    (let [k (first-tasks-key)]
      (= (get-task k) "Task diligently being processed")))

  (testing "Get a nonexistent task"
    (is (nil? (get-task 123)))))

(deftest test-cancel-task
  (testing "Cancel a running task"
    (add-task long-task)
    (let [k (first-tasks-key)]
      (cancel-task k)
      (is (nil? (get-task k)))))

  (testing "Do not cancel a task if complete"
    (add-task test-task)
    (let [k (first-tasks-key)]
      @(get-task-future k)
      (cancel-task k)
      (is (get @ran-tasks k))
      (is (not (future-cancelled? (get-task-future k))))))

  (testing "Do not cancel a task if it does not exist"
    (is (nil? (cancel-task 123)))))