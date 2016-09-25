(ns ontodev.builder.auth.view
  (:require [hiccup.core :refer [html]]
            [ring.util.response :refer [response redirect]]
            [ontodev.builder.layout :as layout]
            [ontodev.builder.auth.oauth :as oauth]
            [ontodev.builder.config :as config]))

(defn login-page
  [{:keys [auth-keys]} errors]
  [:div
   [:h2 "Login"]

   (when (not-empty errors)
     (for [error errors]
       [:div.alert.alert-danger.alert-dismissable
        error]))

   (when (and (:github-client-id auth-keys)
              (:github-client-secret auth-keys))
     [:div
      [:a.btn.btn-primary.btn-block
       {:href "github"}
       "Sign in with Github"]])

   (when (and (:google-client-id auth-keys)
              (:google-client-secret auth-keys))
     [:div
      [:div
       [:a.btn.btn-danger.btn-block
        {:href "google"}
        "Sign in with Google"]]])])

(defn login
  [_ & [errors]]
  (layout/render "login.html" {:content (html (login-page @config/config errors))}))

(defn authenticate
  [{:keys [session] :as req} auth-user-fn user-key]
  (if-let [resp (auth-user-fn req)]
    (-> (redirect "/")
        (assoc :session
               (assoc session :screen-name (get resp user-key)
                              :user-id (:id resp))))

    (login req ["Failed to login, please try again."])))

(def routes
  {""                login

   "github"          oauth/init-github
   "github-callback" #(authenticate % oauth/auth-github oauth/github-user-key)

   "google"          oauth/init-google
   "google-callback" #(authenticate % oauth/auth-google oauth/google-user-key)
   })

(def config
  {:title  "Auth"
   :base   "auth/"
   :routes routes})
