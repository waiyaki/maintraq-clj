(ns maintraq.db.models.audit
  (:require
   [datomic.api :as d]))


(defn create
  "Return a map representing an audit trail transaction for given user."
  [user]
  {:db/id      (d/tempid :db.part/tx)
   :audit/user (:db/id user)})
