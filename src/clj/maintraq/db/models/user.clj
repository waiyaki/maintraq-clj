(ns maintraq.db.models.user
  (:require
   [buddy.hashers :as hashers]
   [datomic.api :as d]
   [maintraq.config :as config :refer [config]]
   [maintraq.db.partition :as db.partition]
   [maintraq.services.mailgun.emails :as emails]
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


(defn confirmation-email
  "Generate confirmation email data from a user."
  [user]
  {:to (:user/email user)
   :subject "Confirm Your Email"
   :text (emails/confirm-registration config user)})


(defn activated?
  "Return `true` if this user account is activated."
  [user]
  (true? (:user/activated user)))


(defn valid-password?
  "Return `true` if the provided password matches the hashed password stored in provided user."
  [user password]
  (true? (hashers/check password (:user/password user))))


(defn admin?
  "Return `true` if this user is an admin."
  [user]
  (= :user.role/admin (:user/role user)))
