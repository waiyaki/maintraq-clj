(ns maintraq.db.schema.audit
  (:require
   [datomic-schema.schema :as datomic.schema :refer [fields schema]]))


(def ^{:schema ::add-audit-trail-schema
       :doc    "Audit trail model schema."}
  audit
  (datomic.schema/generate-schema
   [(schema
     audit
     (fields
      [user :ref
       "User who performed a particular action."]))]
   {:index-all? true}))
