(ns ontodev.builder.utils
  (:require [ring.util.response :as response]))

(defn edn-response
  [body]
  (-> body
      response/response
      (response/content-type "application/edn")))

(defn query-string->map
  [qs]
  (->> (clojure.string/split qs #"&")
       (map #(clojure.string/split % #"="))
       (map (fn [[k v]] [(keyword k) v]))
       (into {})))