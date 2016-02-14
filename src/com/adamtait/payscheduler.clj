(ns com.adamtait.payscheduler
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clj-http.client :as http]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## logging

(def default-logfile-path "/tmp/payscheduler.log")

(defn- log
  [configuration data]
  (let [logfile-path (or (get-in configuration [:log :filepath])
                         default-logfile-path)]
    (try
      (spit logfile-path (pr-str data) :append true)
      (catch Exception e
        (println "got an exception printing to path :" logfile-path)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## configuration

(defn load-configuration
  "returns a clojure hash-map, given a local file path"
  [file-path]
  (-> file-path
      slurp
      edn/read-string))

(defn- validate-payment-configuration
  "returns (identity), after validating the structure & contents"
  [payment-configuration-m]
  (assert (and (contains? payment-configuration-m :from)
               (contains? payment-configuration-m :to)
               (contains? payment-configuration-m :amount)
               (contains? payment-configuration-m :note)))
  (assert (keyword? (:from payment-configuration-m)))
  (assert (keyword? (:to payment-configuration-m)))
  (assert (string? (:amount payment-configuration-m)))
  (assert (string? (:note payment-configuration-m)))
  payment-configuration-m)

(def no-user-name-in-configuration-str
  "please make sure your configuration file contains the user name: ")

(defn- validate-user-configuration
  [users-configuration-m user-k]
  (assert (keyword? user-k))
  (assert (contains? users-configuration-m user-k)
          (str no-user-name-in-configuration-str (name user-k)))
  (let [user-m (get users-configuration-m user-k)]
    (assert (contains? user-m :access_token))
    (assert (or (contains? user-m :phone)
                (contains? user-m :email)
                (contains? user-m :user_id))))
  users-configuration-m)

(defn- validate-configuration
  "returns `configuration` map unchanged, after validating the
  structure & contents"
  [configuration payment-k]
  {:pre [(map? configuration)
         (keyword? payment-k)]}
  (assert (contains? configuration :users))
  (assert (contains? configuration :payments))
  (assert (contains? (:payments configuration) payment-k))
  (validate-payment-configuration (get-in configuration [:payments payment-k]))
  (let [from-user-k (get-in configuration [:payments payment-k :from])
        to-user-k (get-in configuration [:payments payment-k :to])]
    (validate-user-configuration (:users configuration) from-user-k)
    (validate-user-configuration (:users configuration) to-user-k))
  configuration)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Venmo API

(def venmo-api-protocol "https://")
(def venmo-dns-name "api.venmo.com/v1")
(def venmo-payments-path "/payments")

(defn payments-api-uri
  []
  (str venmo-api-protocol venmo-dns-name venmo-payments-path))

(defn- build-form-params
  [configuration payment-k]
  (let [from-user-k (get-in configuration [:payments payment-k :from])
        to-user-k (get-in configuration [:payments payment-k :to])
        amount (Double/parseDouble (get-in configuration [:payments payment-k :amount]))
        note (get-in configuration [:payments payment-k :note])
        access-token (get-in configuration [:users from-user-k :access_token])]
    (merge {:access_token access-token
            :note note
            :amount amount
            :audience "private"}
           (select-keys (get-in configuration [:users to-user-k])
                        [:phone :email :user_id]))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## -main: entry point

(defn -main
  [config-file-path payment-name]
  {:pre [(string? config-file-path)
         (string? payment-name)]}
  (let [payment-k (keyword payment-name)
        configuration (-> config-file-path
                          load-configuration
                          (validate-configuration payment-k))]
    (let [params (build-form-params configuration payment-k)
          response (http/post (payments-api-uri)
                              {:form-params params
                               :throw-exceptions false})]
      (log configuration response)

      (if (= 200 (:status response))
        0
        1))))


(def m {:adam 4
        :tam 6
        "adam" 7})

(get m :adam)
