(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]
   [datomic.api :as d]
   [maintraq.handlers.errors :as errors]))


(defn role [_ _ user]
  (some-> user :user/role name))


(defn full-name [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn retrieve [{:keys [conn] :as ctx} {:keys [id] :as args} _]
  (if-let [user (d/q '[:find ?a .
                         :in $ ?a
                         :where
                         [?a :user/email _]]
                       (d/db conn) id)]
    (d/entity (d/db conn) user)
    (errors/not-found "User not found.")))


(def resolvers
  {:users/full-name full-name
   :users/role      role
   :users/retrieve  retrieve})
