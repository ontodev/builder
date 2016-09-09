(ns ontodev.builder.server
  (:require [ring.adapter.jetty]
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
  (reset! core/config config)
  (ring.adapter.jetty/run-jetty
   (core/make-app config)
   (get config :ring-options default-ring-options)))