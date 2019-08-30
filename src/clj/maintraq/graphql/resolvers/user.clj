(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]))


(defn role [_ _ user]
  (name (:user/role user)))


(defn full-name [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn retrieve [_ args _]
  {:user/username   "waiyaki"
   :user/first-name "James"
   :user/last-name  "Muturi"
   :user/role       :user.role/admin})


(def resolvers
  {:users/full-name full-name
   :users/role      role
   :users/retrieve  retrieve})
