(ns maintraq.seed.users
  (:require
   [buddy.hashers :as hashers]
   [datomic.api :as d]
   [faker.internet]
   [faker.name]
   [maintraq.db.partition :as db.partition]
   [maintraq.utils.core :as ut]))


(defn user
  "Return a map representing a random user."
  ([]
   (user {}))
  ([{:keys [email username first-name last-name middle-name role activation-hash activated password]}]
   (let [first-name (or first-name (faker.name/first-name))
         username   (or username (-> first-name gensym str))]
     (ut/remove-nils
      {:db/id                (db.partition/tempid db.partition/users)
       :user/uid             (d/squuid)
       :user/email           (or email (faker.internet/email username))
       :user/username        username
       :user/first-name      first-name
       :user/middle-name     middle-name
       :user/last-name       (or last-name (faker.name/last-name))
       :user/activated       (if (nil? activated) true activated)
       :user/activation-hash (or activation-hash (d/squuid))
       :user/role            (or role :user.role/member)
       :user/password        (some-> password hashers/derive)}))))


(defn user!
  ([conn]
   (user! conn (user)))
  ([conn user]
   (let [{:keys [db-after tempids]} @(d/transact conn [user])]
     (d/entity db-after (d/resolve-tempid db-after tempids (first (keys tempids)))))))


(defn admin
  "Generate a static admin user"
  ([]
   (admin {}))
  ([opts]
   (user (merge opts
                {:username   "admin"
                 :first-name "admin"
                 :last-name  "MT"
                 :email      "admin@maintraq.com"
                 :role       :user.role/admin}))))
