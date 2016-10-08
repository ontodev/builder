(ns ontodev.builder.layout
  (:require [markdown.core :refer [md-to-html-string]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [response content-type]]
            [selmer.parser :as parser]
            [selmer.filters :as filters]
            [ontodev.builder.config :refer [config]]))

(parser/set-resource-path! (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (-> (parser/render-file
        template
        (assoc params :page template
                      :config @config))
      response
      (content-type "text/html; charset=utf-8")))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})

(def error-404 (error-page {:status 404 :title  "Page not found"}))
(def error-403 (error-page {:status 403 :title "Forbidden access"}))