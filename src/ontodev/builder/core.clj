(ns ontodev.builder.core
  (:require [bidi.ring :refer (make-handler ->Resources)]
            [ring.middleware.session :refer [wrap-session]]
            [ontodev.builder.layout :as layout]))

(defn index
  [request]
  (layout/render "home.html" {:readme (slurp "README.md")}))

(def default-routes
  {"" index
   "assets/" (->Resources {:prefix "assets/"})})

(def base-routes ["/"])

(defn make-app
  "Given the config map,"
  [config]
  (->> config
       :views
       (map (juxt :base :routes))
       (into default-routes)
       (conj base-routes)
       make-handler
       wrap-session))
