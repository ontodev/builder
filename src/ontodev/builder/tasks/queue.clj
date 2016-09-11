(ns ontodev.builder.tasks.queue)

;; TODO: potentially use mount for state management
(def ran-tasks (atom {}))

(defn add-task
  [{task-fn   :var
    task-name :name}]
  (if (and (fn? task-fn)
           (string? task-name))
    (swap! ran-tasks assoc (System/currentTimeMillis) {:task   task-name
                                                       :result (future (task-fn))})
    (throw (RuntimeException. "Malformed task attempted to be added"))))

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