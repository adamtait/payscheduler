(defproject payscheduler "0.1.0-SNAPSHOT"
  :description "a simple app for making a Venmo payment. can be used o schedule payments, in combination with cron."
  :url "http://adamtait.com/payscheduler"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "0.9.2"]])
