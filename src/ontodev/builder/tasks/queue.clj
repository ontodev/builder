(ns ontodev.builder.tasks.queue)

;; A `built.boot` file specifies a number of tasks.
;; A task can be executed multiple times
;; (although we currently allow just one execution at a time).
;; We keep a history of all executions during the current session
;; (no persistence between sessions).

;; TODO: potentially use mount for state management
(def execution-history (atom (sorted-map)))

(defn show-execution
  "Given an execution map,
   return with status and dereferenced results."
  [{:keys [f] :as execution}]
  (when (and execution (future? f))
    (assoc
     (dissoc execution :f)
     :status
     (cond
       (future-cancelled? f) :cancelled
       (future-done? f) :done
       :else :pending)
     :result
     (when (and (future-done? f)
                (not (future-cancelled? f)))
       @f))))

(defn get-execution
  "Given a valid execution ID,
   return a dereferenced execution map."
  [id]
  (when-let [execution (get @execution-history id)]
    (show-execution execution)))

(defn get-executions
  "Get all executions in the history."
  []
  (map show-execution (vals @execution-history)))

(defn get-current-execution
  "Return the first execution in the history that is still pending, or nil."
  []
  (->> @execution-history
       vals
       (filter #(not (future-done? (:f %))))
       first
       show-execution))

(defn execute-task
  "Given a valid Boot task,
   if nothing else is currently executing,
   start a new execution, add it to the history,
   and return a dereferenced execution map."
  [task]
  (cond
    (not (map? task))
    (throw (RuntimeException. "Task is malformed"))

    (not (string? (:name task)))
    (throw (RuntimeException. "Task name not provided"))

    (not (ifn? (:var task)))
    (throw (RuntimeException. "Task function (var) not provided"))

    (get-current-execution)
    (throw (RuntimeException. "Another task is already executing"))

    :else
    (let [id (System/currentTimeMillis)]
      (swap!
       execution-history
       assoc
       id
       {:id id
        :name (:name task)
        :f (future ((:var task)))})
      (get-execution id))))

(defn cancel-execution
  "Given a valid execution ID,
   if it is still pending, cancel it."
  [id]
  (when-let [f (get-in @execution-history [id :f])]
    (when-not (future-done? f)
      (future-cancel f))))

(defn cancel-all-executions
  "Cancel all executions in the history."
  []
  (doseq [id (keys execution-history)]
    (cancel-execution id)))
