(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]
   [datomic.api :as d]
   [maintraq.handlers.errors :as errors]
   [maintraq.graphql.models.user :as user]
   [struct.core :as st]))


(defn- unique [db attr]
  {:message "is unavailable"
   :optional true
   :state true
   :validate (fn [state v]
               (nil? (d/q '[:find ?e .
                            :in $ ?attr ?v
                            :where
                            [?e ?attr ?v]]
                          db attr v)))})


(defn user-schema [db]
  {:username         [st/required st/string (unique db :user/username)]
   :email            [st/required st/email (unique db :user/email)]
   :password         [st/required st/string [st/min-count 8]]
   :confirm_password [st/required st/string [st/min-count 8] [st/identical-to :password]]
   :first_name       [st/string]
   :last_name        [st/string]
   :middle_name      [st/string]})


(defn role [_ _ user]
  (some-> user :user/role name))


(defn full-name [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn retrieve
  "Retrieve a user account by its unique identifier."
  [{:keys [conn] :as ctx} {:keys [id] :as args} _]
  (if-let [user (d/q '[:find ?a .
                       :in $ ?a
                       :where
                       [?a :user/email _]]
                     (d/db conn) id)]
    (d/entity (d/db conn) user)
    (errors/not-found "User not found.")))


(defn create!
  "Create a new member user account."
  [{:keys [conn] :as ctx} {:keys [input] :as args} _]
  (let [[errors] (st/validate input (user-schema (d/db conn)))]
    (if (some? errors)
      (errors/bad-request "Validation error" errors)
      (let [user                       (user/create (assoc input :role :user.role/member))
            {:keys [db-after tempids]} @(d/transact conn [user])]
        (d/entity db-after (d/resolve-tempid db-after tempids (:db/id user)))))))


(def resolvers
  {:users/full-name full-name
   :users/role      role
   :users/retrieve  retrieve
   :users/create!   create!})
