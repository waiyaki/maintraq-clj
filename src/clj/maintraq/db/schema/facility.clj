(ns maintraq.db.schema.facility
  (:require
   [datomic-schema.schema :as datomic.schema :refer [fields schema]]))


(def ^{:schema ::add-facility
       :doc "Facility schema"}
  facility
  (datomic.schema/generate-schema
   [(schema
     facility
     (fields
      [uid :uuid :unique-identity :indexed]
      [name :string :unique-identity :indexed]

      [tasks :ref :many
       "Tasks requested for this facility."]))]
   {:index-all? true}))
