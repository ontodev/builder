(ns ontodev.builder.auth.view
  (:require [hiccup.core :refer [html]]
            [ring.util.response :refer [response redirect]]
            [ontodev.builder.layout :as layout]
            [ontodev.builder.auth.oauth :as oauth]))

(defn authenticate
  [{:keys [session] :as req} auth-user-fn user-key]
  (if-let [resp (auth-user-fn req)]
    (-> (redirect "/")
        (assoc :session
               (assoc session :screen-name (get resp user-key)
                              :user-id (:id resp))))
    ;; TODO: error resp
    ))

(defn login
  [_]
  (layout/render "login.html"))

(def routes
  {""                login

   "github"          (partial oauth/init :github)
   "github-callback" #(authenticate %
                                    (partial oauth/auth-user :github)
                                    oauth/github-user-key)

   "google"          (partial oauth/init :google)
   "google-callback" #(authenticate %
                                    (partial oauth/auth-user :google)
                                    oauth/google-user-key)
   })

(def config
  {:title  "Auth"
   :base   "auth/"
   :routes routes})
