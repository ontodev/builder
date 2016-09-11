(ns ontodev.builder.tasks.view
  (:require [clojure.string :as string]
            [boot.task-helpers]
            [ring.util.response :refer [response]]
            [ontodev.builder.tasks.queue :as queue]
            [ontodev.builder.core :as core]
            [ontodev.builder.utils :refer [edn-response]]))

(defn list-tasks
  []
  (get (#'boot.task-helpers/available-tasks 'boot.user) 'boot.user))

(defn get-task
  [task-name]
  (->> (list-tasks)
       (filter #(= (str (:name %)) task-name))
       first))

(defn run-task
  [{{:keys [name]} :route-params}]
  (let [task      (get-task name)]
    (queue/add-task task)))

(defn cancel-task
  [{{:keys [id]} :route-params}]
  (queue/cancel-task (Long/parseLong id)))

(defn get-ran-task
  [{{:keys [id]} :route-params}]
  (-> id
      Long/parseLong
      queue/get-task
      pr-str
      edn-response))

(defn task-item
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
  [[id {:keys [task result]}]]
  [:li
   [:a {:href id} "RESULT"] [:code task]
   (when-not (future-done? result)
     [:p [:a {:href     "#"
              :onclick (str "cancelTask('" id "');")} "Cancel task"]])])

(defn index
  [_]
  (response
   (core/template
    {:title "TASKS"
     :js ["/assets/tasks.js"]
     :body [:div
            [:h2 "Available Tasks"]
            ;; TODO: clean up the looping logic here
            (->> (list-tasks)
                 (map task-item)
                 (concat [:ul])
                 vec)
            (when (not-empty @queue/ran-tasks)
              [:h2 "Executed Tasks"])
            (->> @queue/ran-tasks
                 (map executed-task-item)
                 (concat [:ul])
                 vec)]})))

(def routes
  {""                      index
   [:id]          {:get    get-ran-task                     ;; get the result of a task
                   :delete cancel-task}                     ;; delete a running task
   [:name "/run"] {:post   run-task}                        ;; run a task
})

(def config
  {:title "Tasks"
   :base "tasks/"
   :routes routes})
