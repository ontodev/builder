(ns ontodev.builder.permissions
  (:require
    [ontodev.builder.config :refer [config]]))

(defn authorized?
  [{{:keys [user-id]} :session} form]
  (or (not (:auth-enabled? @config))
      (case form
        :user user-id
        :admin (contains? (:admins @config) user-id))))

(defn auth-check [handler form]
  (fn [request]
    (if (authorized? request form)
      (handler request)
      (prn "UNAUTH"))))                                     ;;TODO, redirect to error page

(defn wrap-authenticated [handler] (auth-check handler :user))
(defn wrap-admin [handler] (auth-check handler :admin))