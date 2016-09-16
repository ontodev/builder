(ns ontodev.builder.tasks.view
  (:require [boot.task-helpers]
            [ring.util.response :refer [response]]
            [ontodev.builder.layout :as layout]
            [ontodev.builder.tasks.queue :as queue]
            [ontodev.builder.utils :refer [edn-response]]))

(defn list-tasks
  "Returns a list of all possible tasks in the boot.user namespace."
  []
  (get (#'boot.task-helpers/available-tasks 'boot.user) 'boot.user))

(defn get-task
  "Given a task name, returns the first match by name of all
   possible tasks."
  [task-name]
  (->> (list-tasks)
       (filter #(= (str (:name %)) task-name))
       first))

(defn run-task
  "Given a valid task name, finds and exectues it in the queue.
   Returns an empty string."
  [{{task-name :name} :route-params}]
  (let [task (get-task task-name)]
    (queue/execute-task task)
    ;; Bit hacky. Perhaps way to clean up/pass through task info
    ;; Or just not return task on execute-task call
    (response "")))

(defn cancel-task
  "Cancels a task given an id from the request route.
   Returns nil."
  [{{:keys [id]} :route-params}]
  (queue/cancel-execution (Long/parseLong id)))

(defn index
  [_]
  (layout/render "tasks/index.html" {:tasks (list-tasks)}))

(defn executions
  [_]
  (layout/render "tasks/executions.html" {:executions (queue/get-executions)}))

(defn task-executions
  [{{task-name :name} :route-params}]
  (layout/render "tasks/executions.html" {:executions (queue/get-task-executions task-name)
                                          :task-name task-name}))

(defn task-view
  [{{:keys [id]} :route-params}]
  (layout/render "tasks/execution.html" {:execution (queue/get-execution (Long/parseLong id))}))

(def routes
  {""                              index               ;; view all tasks
   "executions"                    executions          ;; view all executions
   ["executions/" :name]           task-executions     ;; view all executions of a task
   ["execution/" :id]     {:get    task-view           ;; view the status and, if applicable, result of a task
                           :delete cancel-task}        ;; cancel a running task
   [:name "/run"]         {:post   run-task}           ;; run a task
})

(def config
  {:title "Tasks"
   :base "tasks/"
   :routes routes})
