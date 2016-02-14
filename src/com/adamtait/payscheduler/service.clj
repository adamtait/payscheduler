(ns com.adamtait.payscheduler.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor.helpers :as interceptor :refer [on-response]]
            [io.pedestal.log :as log]
            [geheimtur.impl.form-based :refer [default-login-handler default-logout-handler]]
            [geheimtur.impl.oauth2 :refer [authenticate-handler callback-handler]]
            [geheimtur.util.auth :as auth :refer [authenticate]]
            [com.adamtait.payscheduler.views :as views]
            [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :as ring-resp]
            [ring.util.codec :as ring-codec]))


(def users
  {"admin" {:name "admin"
            :password "password"
            :roles #{:admin :agent}
            :full-name "Adam Tait"}})

(defn credentials
  [_ {:keys [username password]}]
  (when-let [identity (get users username)]
    (when (= password (:password identity))
      (dissoc identity :password ))))

(def not-found-interceptor
  (on-response
   ::not-found-interceptor
   (fn [response]
     (if-not (ring-resp/response? response)
       (->
        (views/error-page {:title   "Not Found"
                           :message "We are sorry, but the page you are looking for does not exist."})
        (ring-resp/content-type "text/html;charset=UTF-8"))
       response))))

(defn on-github-success
  [_ {:keys [identity return]}]
  (let [user {:name      (:login identity)
              :roles     #{:user}
              :full-name (:name identity)}]
    (->
     (ring-resp/redirect return)
     (authenticate user))))

(defn on-google-success
  [_ {:keys [identity return]}]
  (let [user {:name      (:displayName identity)
              :roles     #{:user}
              :full-name (:displayName identity)}]
    (->
     (ring-resp/redirect return)
     (authenticate user))))

(defn on-venmo-success
  [_ {:keys [identity return]}]
  (let [user {:name      (:login identity)
              :roles     #{:user}
              :full-name (:name identity)}]
    (authenticate
     (ring-resp/redirect return)
     user)))

(def providers
  {:github {:auth-url           "https://github.com/login/oauth/authorize"
            :client-id          (or (System/getenv "github_client_id") "client-id")
            :client-secret      (or (System/getenv "github_client_secret") "client-secret")
            :scope              "user:email"
            :token-url          "https://github.com/login/oauth/access_token"
            ;; use a custom function until (and if) https://github.com/dakrone/clj-http/pull/264 is merged
            :token-parse-fn     #(-> % :body ring-codec/form-decode keywordize-keys)
            :user-info-url      "https://api.github.com/user"
            ;; it is not really needed but serves as an example of how to use a custom parser
            :user-info-parse-fn #(-> % :body (parse-string true))
            :on-success-handler on-github-success}
   :google {:auth-url           "https://accounts.google.com/o/oauth2/auth"
            :client-id          (or (System/getenv "google_client_id") "client-id")
            :client-secret      (or (System/getenv "google_client_secret") "client-secret")
            :callback-uri       "http://payscheduler.adamtait.com/oauth.callback"
            :scope              "profile email"
            :token-url          "https://accounts.google.com/o/oauth2/token"
            :user-info-url      "https://www.googleapis.com/plus/v1/people/me"
            :on-success-handler on-google-success}
   :venmo {:auth-url           "https://api.venmo.com/v1/oauth/authorize"
           :client-id          (or (System/getenv "venmo_client_id") "client-id")
           :client-secret      (or (System/getenv "venmo_client_secret") "client-secret")
           :callback-uri       "http://payscheduler.adamtait.com/oauth.callback"
           :scope              "make_payments"
           :token-url          "https://api.venmo.com/v1/oauth/access_token"
           :user-info-url      "https://venmo.com/adamtait"
           :on-success-handler on-venmo-success}})

(defroutes routes
  [[["/" {:get views/home-page}
     ^:interceptors [(body-params/body-params)
                     bootstrap/html-body]
     ["/login" {:get views/login-page :post (default-login-handler {:credential-fn credentials})}]
     ["/logout" {:get default-logout-handler}]
     ["/oauth.login" {:get (authenticate-handler providers)}]
     ["/oauth.callback" {:get (callback-handler providers)}]
     ["/unauthorized" {:get views/unauthorized}]]]])

(def service
  (bootstrap/default-interceptors
    {:env :prod
     ::bootstrap/routes routes
     ::bootstrap/resource-path "/public"
     ::bootstrap/not-found-interceptor not-found-interceptor
     ::bootstrap/type :jetty
     ::bootstrap/enable-session {:cookie-name "SID"
                                 :store (cookie/cookie-store)}
     ::bootstrap/port (Integer/valueOf (or (System/getenv "PORT") "80"))}))
