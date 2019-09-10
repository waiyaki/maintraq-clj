(ns maintraq.seed.users
  (:require
   [datomic.api :as d]
   [faker.internet]
   [faker.name]
   [maintraq.db.partition :as db.partition]))


(defn user
  "Return a map representing a random user."
  ([]
   (user {}))
  ([{:keys [email username first-name last-name middle-name role activation-hash activated]}]
   (let [first-name (or first-name (faker.name/first-name))
         username   (or username (-> first-name gensym str))]
     (merge {:db/id                (db.partition/tempid db.partition/users)
             :user/uid             (d/squuid)
             :user/email           (or email (faker.internet/email username))
             :user/username        username
             :user/first-name      first-name
             :user/last-name       (or last-name (faker.name/last-name))
             :user/activated       (or activated true)
             :user/activation-hash (or activation-hash (d/squuid))
             :user/role            (or role :user.role/member)}
            (when (some? middle-name)
              {:user/middle-name middle-name})))))


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
