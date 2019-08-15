(ns maintraq.db.core
  (:require
   [datomic.api :as d]
   [mount.core :refer [defstate]]
   [maintraq.config :as config :refer [config]]))


(defn connect! [uri]
  (d/create-database uri)
  (d/connect uri))


(defstate conn
  :start (connect! (config/datomic-uri config))
  :stop  (d/release conn))
