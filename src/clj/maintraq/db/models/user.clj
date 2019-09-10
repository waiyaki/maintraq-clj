(ns maintraq.db.models.user
  (:require
   [buddy.hashers :as hashers]
   [datomic.api :as d]
   [maintraq.db.partition :as db.partition]
   [maintraq.utils.core :as ut]))


(defn create
  "Generate a map representing a new user."
  [{:keys [username email first_name last_name middle_name password role]}]
  (merge {:db/id                (db.partition/tempid db.partition/users)
          :user/email           email
          :user/username        username
          :user/role            role
          :user/activated       false
          :user/uid             (d/squuid)
          :user/activation-hash (d/squuid)
          :user/password        (hashers/derive password)}
         (ut/remove-nils {:user/first-name  first_name
                          :user/last-name   last_name
                          :user/middle-name middle_name})))
