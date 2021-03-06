(ns maintraq.graphql.resolvers.user
  (:require
   [clojure.string :as str]
   [datomic.api :as d]
   [maintraq.auth.core :as auth]
   [maintraq.config :as config :refer [config]]
   [maintraq.handlers.errors :as errors]
   [maintraq.db.models.user :as user]
   [maintraq.services.mailgun.core :as mailgun]
   [maintraq.validation.schema :as validation.schema]
   [struct.core :as st]))


(defn ^{:authorized? (constantly true)}
  create!
  "Create a new member user account."
  [{:keys [conn] :as ctx} {:keys [input] :as args} _]
  (let [[errors] (st/validate input (validation.schema/user (d/db conn)))]
    (if (some? errors)
      (errors/bad-request "Validation error" errors)
      (let [user-data                  (user/create (assoc input :role :user.role/member))
            {:keys [db-after tempids]} @(d/transact conn [user-data])
            user                       (d/entity
                                        db-after
                                        (d/resolve-tempid db-after tempids (:db/id user-data)))]
        (mailgun/send-email! config (user/confirmation-email user))
        user))))


(defn ^{:authorized? (constantly true)}
  activate!
  "Activate a user account."
  [{:keys [conn] :as ctx} {:keys [input] :as args} _]
  (let [{:keys [uid activation_hash]} input
        activation-hash               (java.util.UUID/fromString activation_hash)
        user-id                       (d/q '[:find ?e .
                                             :in $ ?uid ?hash
                                             :where
                                             [?e :user/uid ?uid]
                                             [?e :user/activation-hash ?hash]]
                                           (d/db conn) uid activation-hash)
        user                          (some->> user-id (d/entity (d/db conn)))]
    (cond
      (nil? user)            (errors/bad-request "Invalid user/hash combination.")
      (user/activated? user) (errors/conflict "Account is already active.")
      :else
      (let [{:keys [db-after]} @(d/transact conn [{:db/id          user-id
                                                   :user/activated true}])]
        (d/entity db-after user-id)))))


(defn ^{:authorized? (constantly true)}
  login
  "Log a user in. Return a JWT token when login is successful."
  [{:keys [conn] :as ctx} {{:keys [username password] :as input} :input :as args} _]
  (let [user (d/entity (d/db conn) [:user/username username])]
    (cond
      (nil? user)                     (errors/unauthorized "Invalid username/password combination.")
      (false? (user/valid-password?
               user
               password))             (errors/unauthorized
                                       "Invalid username/password combination.")
      (false? (user/activated? user)) (errors/forbidden "This user account is inactive.")
      :else                           {:token (auth/sign {:uid (:user/uid user)})})))


(defn ^{:authorized? (constantly true)}
  role
  "Extract a user's role name."
  [_ _ user]
  (some-> user :user/role name))


(defn ^{:authorized? (constantly true)}
  full-name
  "Construct a user's full name, if available."
  [_ _ user]
  (->> (select-keys user [:user/first-name :user/middle-name :user/last-name])
       vals
       (remove nil?)
       (str/join " ")
       not-empty))


(defn
  ^{:authorized?         (fn ^{:doc "Users can retrieve own accounts"}
                           [{:keys [db requester]} {{:keys [username]} :input} _]
                           (let [user (d/entity db [:user/username username])]
                             (= (:user/uid requester) (:user/uid user))))
    :authorization-error (errors/not-found "User not found.")}
  retrieve
  "Retrieve a user account by its unique identifier."
  [{:keys [conn] :as ctx} {{:keys [username] :as input} :input :as args} _]
  (if-some [user (d/entity (d/db conn) [:user/username username])]
    user
    (errors/not-found "User not found.")))


(def resolvers
  {;; Queries
   :users/full-name full-name
   :users/role      role
   :users/retrieve  retrieve

   ;; Mutations
   :users/create!   create!
   :users/activate! activate!
   :users/login     login})
