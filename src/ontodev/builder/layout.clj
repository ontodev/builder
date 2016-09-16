(ns ontodev.builder.layout
  (:require [markdown.core :refer [md-to-html-string]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [response content-type]]
            [selmer.parser :as parser]
            [selmer.filters :as filters]
            [ontodev.builder.config :refer [env]]))

(parser/set-resource-path! (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (-> (parser/render-file
        template
        (assoc params :page   template
                      :config env))
      response
      (content-type "text/html; charset=utf-8")))