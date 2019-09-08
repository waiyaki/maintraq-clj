(ns maintraq.db.core
  (:require
   [datomic.api :as d]
   [mount.core :refer [defstate]]
   [maintraq.config :as config :refer [config]]
   [maintraq.db.schema :as schema]
   [maintraq.seed.core :as seed]
   [taoensso.timbre :as timbre]))


(defn connect! [uri]
  (timbre/info ::connecting {:uri uri})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (schema/conform-schema! conn)
    (seed/seed! conn)
    conn))


(defstate conn
  :start (connect! (config/datomic-uri config))
  :stop  (d/release conn))
