(ns ontodev.builder.utils
  (:require [ring.util.response :as response]))

(defn edn-response
  [body]
  (-> body
      response/response
      (response/content-type "application/edn")))