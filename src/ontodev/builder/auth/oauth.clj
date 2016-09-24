(ns ontodev.builder.auth.oauth
  (:require [oauth.github :as github]
            [oauth.google :as google]
            [ring.util.response :refer [redirect]]
            [ontodev.builder.utils :refer [query-string->map]]))


;; TODO: FOR TESTING ONLY. REMOVE AND DROP IN CONFIG LATER
(def ^:const github-client-id "")
(def ^:const github-client-secret "")

(def ^:const google-client-id "")
(def ^:const google-client-secret "")
;; END TESTING BIT

(def ^:const github-user-key :name)
(def ^:const google-user-key :displayName)

(defn unsupported-protocol
  []
  (throw (RuntimeException. "No such oauth protocol supported")))

(defn callback-uri
  [{:keys [headers scheme]} suffix]
  (str (name scheme) "://" (get headers "host") "/auth/" suffix))

(defmulti init (fn [protocol _] protocol))

(defmethod init :github
  [_ req]
  (redirect (github/oauth-authorization-url github-client-id (callback-uri req "github-callback"))))

(defmethod init :google
  [_ req]
  (redirect (google/oauth-authorization-url google-client-id (callback-uri req "google-callback"))))

(defmethod init :default [_ _] (unsupported-protocol))


(defmulti callback (fn [protocol _] protocol))

(defmethod callback :github
  [_ {:keys [query-string] :as req}]
  (github/oauth-access-token github-client-id
                             github-client-secret
                             (-> query-string query-string->map :code)
                             (callback-uri req "github-callback")))

(defmethod callback :google
  [_ {:keys [query-string] :as req}]
  (google/oauth-access-token google-client-id
                             google-client-secret
                             (-> query-string query-string->map :code)
                             (callback-uri req "google-callback")))

(defmethod callback :default [_ _] (unsupported-protocol))


(defmulti get-user (fn [protocol _] protocol))

(defmethod get-user :github
  [_ access-token]
  ((github/oauth-client access-token)
    {:method :get
     :url    "https://api.github.com/user"}))

(defmethod get-user :google
  [_ access-token]
  ((google/oauth-client access-token)
    {:method :get
     :url    "https://www.googleapis.com/plus/v1/people/me"}))

(defmethod get-user :default [_ _] (unsupported-protocol))

(defn auth-user
  [protocol req]
  (let [{:keys [access-token]} (callback protocol req )]
    (when access-token
      (get-user protocol access-token))))