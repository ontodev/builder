(ns ontodev.builder.auth.view
  (:require [hiccup.core :refer [html]]
            [ring.util.response :refer [response redirect]]
            [ontodev.builder.layout :as layout]
            [ontodev.builder.auth.oauth :as oauth]
            [ontodev.builder.config :as config]))

(defn login-page
  [config {:keys [user-id screen-name]} errors]
  (if user-id
    [:div
     [:h2 "You are logged in as " screen-name]
     [:a.btn.btn-default.btn-block
      {:href "logout"}
      "Logout"]]
    [:div
     [:h2 "Login"]

     (when (not-empty errors)
       (for [error errors]
         [:div.alert.alert-danger.alert-dismissable
          error]))

     (when (and (:github-client-id config)
                (:github-client-secret config))
       [:div
        [:a.btn.btn-primary.btn-block
         {:href "github"}
         "Sign in with Github"]])

     (when (and (:google-client-id config)
                (:google-client-secret config))
       [:div
        [:div
         [:a.btn.btn-danger.btn-block
          {:href "google"}
          "Sign in with Google"]]])]))

(defn login
  [{:keys [session]} & [errors]]
  (layout/render "login.html" {:content (html (login-page @config/config session errors))}))

(defn authenticate
  [{:keys [session] :as req} auth-user-fn user-key source]
  (if-let [resp (auth-user-fn req)]
    (-> (redirect "/")
        (assoc :session
               (assoc session :screen-name (get resp user-key)
                              :user-id     (:id resp)
                              :auth-source source)))

    (login req ["Failed to login, please try again."])))

(defn logout
  [{:keys [session]}]
  (-> (redirect "/")
      (assoc :session
             (dissoc session :screen-name :user-id :auth-source))))

(def routes
  {""                login

   "github"          oauth/init-github
   "github-callback" #(authenticate % oauth/auth-github oauth/github-user-key :github)

   "google"          oauth/init-google
   "google-callback" #(authenticate % oauth/auth-google oauth/google-user-key :google)

   "logout"          logout
   })

(def config
  {:title  "Auth"
   :base   "auth/"
   :routes routes})
