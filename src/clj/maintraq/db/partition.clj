(ns maintraq.db.partition
  (:require
   [datomic.api :as d]
   [datomic-schema.schema :as datomic.schema]))


(defn tempid
  "Create a new Datomic `tempid` in the given partition"
  [part]
  (d/tempid part))


(defn db-part [entity]
  (datomic.schema/part (str "maintraq." entity)))


(def users (db-part "users"))
(def facilities (db-part "facilities"))


(def ^{:doc "Schema for partition in which to group users."
       :schema ::users}
  user-partition
  (datomic.schema/generate-parts [users]))


(def ^{:doc "Schema for partition in which to group facilities."
       :schema ::facilities}
  facilities-partition
  (datomic.schema/generate-parts [facilities]))
