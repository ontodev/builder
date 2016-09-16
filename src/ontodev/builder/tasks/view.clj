(ns ontodev.builder.tasks.view
  (:require [clojure.string :as string]
            [boot.task-helpers]
            [ring.util.response :refer [response]]
            [ontodev.builder.tasks.queue :as queue]
            [ontodev.builder.core :as core]
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

(defn get-ran-task
  [{{:keys [id]} :route-params}]
  (-> id
      Long/parseLong
      queue/get-execution
      pr-str
      edn-response))

(defn task-item
  "Given a single task, returns the list template for rendering."
  [{:keys [name doc argspec] :as task}]
  [:li
   [:a {:href "#"
        :onclick (str "createTask('" name "');")} "RUN"]
   " "
   [:code name]
   [:p (-> doc (string/replace #"(?s)Keyword Args:.*" "") string/trim)]
   ;[:pre (str argspec)]
])

(defn executed-task-item
  "Given a single executed task item, returns
   the list template for rendering."
  [{id        :id
    task-name :name
    status    :status}]
  [:li
   [:code task-name]
   [:span " - "]
   (case status
     :done
     [:a
      {:href id}
      "RESULT"]

     :pending
     [:a
      {:href    "#"
       :onclick (str "cancelTask('" id "');")}
      "CANCEL"]

     :cancelled
     [:span "CANCELLED"])])

(defn index
  [_]
  (response
   (core/template
    {:title "TASKS"
     :js ["/assets/tasks.js"]
     :body [:div
            [:h2 "Available Tasks"]
            [:ul
             (map task-item (list-tasks))]

            (when (not-empty (queue/get-executions))
              [:h2 "Executed Tasks"])
            [:ul
             (map executed-task-item (queue/get-executions))]]})))

(def routes
  {""                      index                            ;; view all tasks
   "/executions"           index                            ;; view all executions
   ["/executions/" :name]  index                            ;; view all executions of a task
   [:id]          {:get    get-ran-task                     ;; view the status and, if applicable, result of a task
                   :delete cancel-task}                     ;; cancel a running task
   [:name "/run"] {:post   run-task}                        ;; run a task
})

(def config
  {:title "Tasks"
   :base "tasks/"
   :routes routes})
