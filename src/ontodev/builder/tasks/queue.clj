(ns ontodev.builder.tasks.queue)

(def ran-tasks (atom {}))

(defn add-task
  [{task-fn   :var
    task-name :name}]
  (swap! ran-tasks assoc (System/currentTimeMillis) {:task   task-name
                                                     :result (future (task-fn))}))

(defn drop-task
  [id]
  (swap! ran-tasks dissoc id))

(defn get-task-future
  [id]
  (->> id
       (get @ran-tasks)
       :result))

(defn get-task
  [id]
  (when-let [f (get-task-future id)]
    (cond
      (future-cancelled? f) "Task cancelled!"
      (future-done? f) @f
      :else "Task diligently being processed")))

(defn cancel-task
  [id]
  (when-let [f (get-task-future id)]
    (when-not (future-done? f)
      (future-cancel f)
      (drop-task id))))