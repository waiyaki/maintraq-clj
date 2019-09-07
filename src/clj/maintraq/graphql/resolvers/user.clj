(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]
   [datomic.api :as d]))


(defn role [_ _ user]
  (name (:user/role user)))


(defn full-name [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn retrieve [{:keys [conn] :as ctx} {:keys [id] :as args} _]
  (d/entity (d/db conn) id))


(def resolvers
  {:users/full-name full-name
   :users/role      role
   :users/retrieve  retrieve})
