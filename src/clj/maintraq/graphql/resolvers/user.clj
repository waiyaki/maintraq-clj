(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]))


(defn role [_ _ user]
  (:user/role user))


(defn full-name [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn user-by-id [_ args _]
  {:user/username   "waiyaki"
   :user/first-name "James"
   :user/last-name  "Muturi"})


(def resolvers
  {:user/full-name  full-name
   :user/role       role
   :user/user-by-id user-by-id})
