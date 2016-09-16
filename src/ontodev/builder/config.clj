(ns ontodev.builder.config
  (:require [mount.core :refer [args defstate]]))

(defstate env :start (args))