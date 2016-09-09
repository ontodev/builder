(ns ontodev.builder.core
  (:require [markdown.core :refer [md-to-html-string]]
            [net.cgrand.enlive-html :as enlive]
            [bidi.ring :refer (make-handler ->Resources)]
            [ring.util.response :refer [response]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.session :refer [wrap-session]]))

(def config (atom {}))

(defn include-js
  [src]
  (first (enlive/html [:script {:src src}])))

(defn include-css
  [href]
  (first (enlive/html [:link {:href href :rel "stylesheet"}])))

(defn view-button
  [view]
  [:li [:a {:href (str "/" (:base view))} (:title view)]])

(defn build-nav
  [config]
  (first
   (enlive/html
    [:nav.navbar.navbar-default.navbar-static-top
     [:div.container
      [:div.navbar-header
       [:a.navbar-brand
        {:href "/"}
        (str (:organization config)
             " / "
             (:project config))]]
      [:div#navbar.collapse.navbar-collapse
       [:div.nav.navbar-nav
        (apply conj
               [:ul.nav.navbar-nav]
               (map view-button (:views config)))]]]])))

(enlive/deftemplate template "template.html"
  [{:keys [title body css js] :as content}]
  [:head :title] (enlive/content title)
  [:head] (enlive/append (map include-css css))
  [:body :nav] (enlive/substitute (build-nav @config))
  [:#view] (if (string? body)
             (enlive/html-content body)
             (enlive/content (enlive/html body)))
  [:body] (enlive/append (map include-js js))
  [:#X-CSRF-Token] (enlive/html-content (anti-forgery-field)))

(defn index
  [request]
  (response
   (template
    {:title "INDEX"
     :body (md-to-html-string (slurp "README.md"))})))

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
