(ns maintraq.db.schema.user
  (:require
   [datomic-schema.schema :as datomic.schema :refer [fields schema]]
   [maintraq.db.partition :as db.partition]))


(def user-roles #{:user.role/admin
                  :user.role/member
                  :user.role/maintenance})


(def ^{:schema ::add-roles
       :doc    "User roles"}
  roles
  (mapv (fn [role]
          {:db/id    (db.partition/tempid :db.part/db)
           :db/ident role})
        user-roles))


(def ^{:schema   ::add-user
       :requires [::add-roles]
       :doc      "User schema"}
  user
  (datomic.schema/generate-schema
   [(schema
     user
     (fields
      [uid :uuid :unique-identity :indexed]
      [email :string :unique-identity :indexed]
      [username :string :unique-identity :indexed]
      [first-name :string :indexed]
      [middle-name :string :indexed]
      [last-name :string :indexed]

      [password :string
       "User's hashed password"]

      [activation-hash :uuid
       "User's activation  hash, generated at signup."]

      [activated :boolean
       "User's activation status, true after activation."]

      [role :enum
       "Role of this user account."]))]
   {:index-all? true}))
