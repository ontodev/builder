(ns ontodev.builder.tasks.view
  (:require
    [bidi.ring :refer [wrap-middleware]]
    [boot.task-helpers]
    [hiccup.core :refer [html]]
    [ring.util.response :refer [response]]
    [ontodev.builder.layout :as layout]
    [ontodev.builder.permissions :as permissions]
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
  [{task-name :name} admin?]
  [:li
   [:a
    {:href    "#"
     :onclick (when admin? (str "createTask('" task-name "');"))}
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
  [admin?]
  [:div
   [:h2 "Available Tasks"]
   [:ul
    (for [task (list-tasks)]
      (task-item task admin?))]
   [:a {:href "/tasks/executions"} "Executions listing"]])

(defn execution-item
  [{id             :id
    execution-name :name
    status         :status}
   admin?]
  [:li
   [:a {:href (str "/tasks/execution/" id)}
    [:code execution-name]]
   (when (= status :pending)
     [:a {:href    "#"
         :onclick (when admin? (str "cancelTask('" id "');"))}])])

(defn executions-listing
  [executions admin? & [task-name]]
  [:div
   [:h2 (if task-name
          (str task-name " Executions")
          "Executions")]
   [:ul
    (for [execution executions]
      (execution-item execution admin?))]
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
  "View all tasks"
  [req]
  (render-tasks-page "TASKS" (tasks-listing (permissions/authorized? req :admin))))

(defn executions
  "View all executions"
  [req]
  (render-tasks-page "TASKS - Executions" (executions-listing (queue/get-executions)
                                                              (permissions/authorized? req :admin))))

(defn task-executions
  "View all executions of a task"
  [{:keys [route-params] :as req}]
  (let [task-name (:name route-params)]
    (render-tasks-page (str task-name " - Executions")
                       (executions-listing (queue/get-task-executions task-name)
                                           (permissions/authorized? req :admin)
                                           task-name))))

(defn task-view
  "View the status and, if applicable, result of a task"
  [{{:keys [id]} :route-params}]
  (let [{:keys [name] :as execution} (queue/get-execution (Long/parseLong id))]
    (render-tasks-page name
                       (execution-view execution))))

(def routes
  {""                    index
   "executions"          executions
   ["executions/" :name] task-executions
   ["execution/" :id]    {:get    task-view
                          :delete (wrap-middleware cancel-task permissions/wrap-admin)}
   [:name "/run"]        {:post (wrap-middleware run-task permissions/wrap-admin)}
   })

(def config
  {:title  "Tasks"
   :base   "tasks/"
   :routes (wrap-middleware routes permissions/wrap-authenticated)})
