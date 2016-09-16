(ns ontodev.builder.server
  (:require [mount.core :as mount]
            [ring.adapter.jetty]
            [ontodev.builder.config :refer [env]]
            [ontodev.builder.core :as core]))

(defn validate-config
  [config]
  (println "Configuration is valid"))

(def default-ring-options
  {:port 3001})

(defn serve!
  "Given a Ring options map and zero or more view maps,
   save the views to the config atom,
   and run a BUILDer server with those views."
  [config]
  (validate-config config)
  (mount/start-with-args config #'ontodev.builder.config/env)
  (ring.adapter.jetty/run-jetty
   (core/make-app env)
   (get config :ring-options default-ring-options)))
