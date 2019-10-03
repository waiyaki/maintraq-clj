(ns maintraq.seed.facilities
  (:require
   [datomic.api :as d]
   [faker.address]
   [maintraq.db.partition :as db.partition]))


(defn facility
  "Return a map representing a facility."
  ([] (facility {}))
  ([{:keys [name] :as opts}]
   (let [name (or name (format "%s, %s"
                               (faker.address/street-address)
                               (faker.address/secondary-address)))]
     {:db/id         (db.partition/tempid db.partition/facilities)
      :facility/uid  (d/squuid)
      :facility/name name})))


(defn facility!
  ([conn] (facility! conn (facility)))
  ([conn facility]
   (let [{:keys [db-after tempids]} @(d/transact conn [facility])]
     (d/entity db-after (d/resolve-tempid db-after tempids (first (keys tempids)))))))
