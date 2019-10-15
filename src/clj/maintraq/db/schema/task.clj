(ns maintraq.db.schema.task
  (:require
   [datomic-schema.schema :as datomic.schema :refer [fields schema]]
   [maintraq.db.partition :as db.partition]))


(def ^{:schema ::add-task-statuses
       :doc    "Task work progress statuses."}
  statuses
  (mapv (fn [status]
          {:db/id    (db.partition/tempid :db.part/db)
           :db/ident status})
        #{:task.status/initial
          :task.status/confirmed
          :task.status/acknowledged
          :task.status/started
          :task.status/pending
          :task.status/done}))


(def ^{:schema   ::add-task
       :requires [::add-task-statuses]
       :doc      "Task model schema."}
  task
  (datomic.schema/generate-schema
   [(schema
     task
     (fields
      [uid :uuid :unique-identity]
      [title :string :fulltext]
      [description :string :fulltext]
      [status :enum
       "Work status of this task."]
      [requester :ref
       "User who requested this maintenance task."]
      [assignee :ref
       "User assigned to this maintenance task."]))]
   {:index-all? true}))
