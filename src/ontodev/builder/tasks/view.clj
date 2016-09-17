(ns ontodev.builder.tasks.view
  (:require [boot.task-helpers]
            [hiccup.core :refer [html]]
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

(defn task-item
  [{task-name :name}]
  [:li
   [:a
    {:href "#"
     :onclick (str "createTask('" task-name "');")}
    [:code task-name]]
   [:p
    [:a
     {:href (str "executions/" task-name)}
     "Executions"]]
   ; TODO: arg for tasks
   ;[:p (-> doc (string/replace #"(?s)Keyword Args:.*" "") string/trim)]
   ;[:pre (str argspec)]
   ])

(defn tasks-listing
  []
  [:div
   [:h2 "Available Tasks"]
   [:ul
    (for [task (list-tasks)]
      (task-item task))]
   [:a {:href "/tasks/executions"} "Executions listing"]])

(defn execution-item
  [{id             :id
    execution-name :name}]
  [:li
   [:a {:href (str "execution/" id)}
    [:code execution-name]]])

(defn executions-listing
  [executions & [task-name]]
  [:div
   [:h2 (if task-name
          (str task-name " Executions")
          "Executions")]
   [:ul
    (for [execution executions]
      (execution-item execution))]
   [:a {:href "/tasks/"} "Back"]])

(defn execution-view
  [{execution-name :name
    status         :status
    result         :result}]
  [:div
   [:h2 execution-name]
   [:p (str "Status: " (name status))]
   (when (= status :done)
     [:div
      [:h4 "Result"]
      [:code result]])
   [:a {:href "/tasks/"} "Back"]])

(defn render-tasks-page
  [title content]
  (layout/render "tasks.html" {:title   title
                               :content (html content)}))

(defn index
  [_]
  (render-tasks-page "TASKS" (tasks-listing)))

(defn executions
  [_]
  (render-tasks-page "TASKS - Executions" (executions-listing (queue/get-executions))))

(defn task-executions
  [{{task-name :name} :route-params}]
  (render-tasks-page (str task-name " - Executions")
                     (executions-listing (queue/get-task-executions task-name) task-name)))

(defn task-view
  [{{:keys [id]} :route-params}]
  (let [{:keys [name] :as execution} (queue/get-execution (Long/parseLong id))]
    (render-tasks-page name
                       (execution-view execution))))

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
