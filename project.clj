(defproject payscheduler "0.1.0-SNAPSHOT"
  :description
  "a simple app for making a Venmo payment. can be used to schedule
  payments, in combination with cron."
  :url "http://payscheduler.adamtait.com/"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "0.9.2"]
                 
                 [io.pedestal/pedestal.service "0.4.1"]
                 [io.pedestal/pedestal.jetty "0.4.1"]
                 [io.pedestal/pedestal.service-tools "0.4.1"]
                 
                 [geheimtur "0.3.0"]
                 
                 [hiccup "1.0.4"]
                 [cheshire "5.2.0"]]
  :main ^:skip-aot com.adamtait.payscheduler.server)
