(ns maintraq.db.models.facility
  (:require
   [datomic.api :as d]
   [maintraq.db.partition :as db.partition]))


(defn create
  "Generate a map representing a new facility."
  [{:keys [name]}]
  {:db/id         (d/tempid db.partition/facilities)
   :facility/uid  (d/squuid)
   :facility/name name})
