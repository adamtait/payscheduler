(ns com.adamtait.payscheduler.auth.service
  (:require [clojure.java.io :as io]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]))

(defn home-page
  [request]
  (ring-resp/response
   (io/resource "public/home.html")))

(defn venmo-url
  [client-id]
  (format
   "https://api.venmo.com/v1/oauth/authorize?client_id=%s&scope=make_payments&response_type=code"
   client-id))

(defn redirect-to-venmo
  [request]
  (let [client-id (get-in request [:path-params :clientId])]
    (ring-resp/redirect
     (venmo-url client-id))))

(def access-token-uri
  "https://api.venmo.com/v1/oauth/access_token")

(defn access-token-request
  [code]
  {:client_id
   :code code
   :client_secret })

(defn oauth-response
  [request]
  (let [code (get-in request [:path-params :code])
        resp (access-token-request code)]))

(defroutes routes
  [[["/" home-page
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params)]
     ["/begin-venmo-auth" {:post redirect-to-venmo}]

     ["/oauth" {:get oauth-response}]]]])

;; Consumed by gzip.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ;;::bootstrap/resource-path "/public"
              ::bootstrap/type :jetty

              ::bootstrap/port 80})
