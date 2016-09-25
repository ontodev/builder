(ns ontodev.builder.auth.oauth
  (:require [ring.util.response :refer [redirect]]
            [oauth.github :as github]
            [oauth.google :as google]
            [ontodev.builder.utils :refer [query-string->map]]
            [ontodev.builder.config :refer [config]]))

(def ^:const github-user-key :name)
(def ^:const google-user-key :display-name)

(def ^:const github-user-req {:method :get
                              :url    "https://api.github.com/user"})

(def ^:const google-user-req {:method :get
                              :url    "https://www.googleapis.com/plus/v1/people/me"})

(defn- callback-uri
  [{:keys [headers scheme]} suffix]
  (str (name scheme) "://" (get headers "host") "/auth/" suffix))

(defn- init
  [oauth-authorization-url client-id suffix req]
  (redirect (oauth-authorization-url client-id (callback-uri req suffix))))

(defn- callback
  [oauth-access-token client-id client-secret suffix {:keys [query-string] :as req}]
  (oauth-access-token client-id
                      client-secret
                      (-> query-string query-string->map :code)
                      (callback-uri req suffix)))

(defn- get-user
  [oauth-client user-req access-token]
  ((oauth-client access-token)
    user-req))

(defn- auth-user
  [{:keys [oauth-access-token oauth-client client-id client-secret suffix user-req]} req]
  (let [{:keys [access-token]} (callback oauth-access-token client-id client-secret suffix req)]
    (when access-token
      (get-user oauth-client user-req access-token))))

;; GITHUB

(defn init-github
  [req]
  (init github/oauth-authorization-url
        (get-in @config [:auth-keys :github-client-id])
        "github-callback"
        req))

(defn auth-github
  [req]
  (auth-user {:oauth-access-token github/oauth-access-token
              :oauth-client       github/oauth-client
              :client-id          (get-in @config [:auth-keys :github-client-id])
              :client-secret      (get-in @config [:auth-keys :github-client-secret])
              :suffix             "github-callback"
              :user-req           github-user-req}
             req))

;; GOOGLE

(defn init-google
  [req]
  (init google/oauth-authorization-url
        (get-in @config [:auth-keys :google-client-id])
        "google-callback"
        req))

(defn auth-google
  [req]
  (auth-user {:oauth-access-token google/oauth-access-token
              :oauth-client       google/oauth-client
              :client-id          (get-in @config [:auth-keys :google-client-id])
              :client-secret      (get-in @config [:auth-keys :google-client-secret])
              :suffix             "google-callback"
              :user-req           google-user-req}
             req))