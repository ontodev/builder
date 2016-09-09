(ns ontodev.builder.tasks.view
  (:require [clojure.string :as string]
            [boot.task-helpers]
            [ring.util.response :refer [response]]
            [ontodev.builder.tasks.queue :as queue]
            [ontodev.builder.core :as core]
            [ontodev.builder.utils :refer [edn-response]]
            [ontodev.builder.js-utils :as js]))

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
        :onclick (js/post {:url (str name "/run")
                           :on-success (str "function(resp) {" js/reload "}")})} "RUN"]
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
              :onclick (js/delete {:url        id
                                   :on-success (str "function(resp) {" js/reload "}")})} "Cancel task"]])])

(defn index
  [_]
  (response
   (core/template
    {:title "TASKS"
     ;:js ["/assets/task.js"] ;; TODO: eventually actually have a js file instead of clojure js strings...
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
