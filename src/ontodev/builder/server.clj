(ns ontodev.builder.server
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty]
            [ontodev.builder.config :as config]
            [ontodev.builder.core :as core]))

(defn validate-config
  [config]
  (println "Configuration is valid"))

(defn str-key->config-key
  [config k]
  (update config k read-string))

(defn format-config
  [{:keys [auth-enabled? admins]
    :as config}]
  (cond-> config
          auth-enabled? (str-key->config-key :auth-enabled?)
          admins        (str-key->config-key :admins)))

(def default-ring-options
  {:port 3001})

(defn serve!
  "Given a Ring options map and zero or more view maps,
   save the views to the config atom,
   and run a BUILDer server with those views."
  [config]
  (let [config (format-config (merge config env))]
    (validate-config config)
    (reset! config/config config)
    (ring.adapter.jetty/run-jetty
      (core/make-app config)
      (get config :ring-options default-ring-options))))
