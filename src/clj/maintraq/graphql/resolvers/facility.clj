(ns maintraq.graphql.resolvers.facility
  (:require
   [datomic.api :as d]
   [maintraq.auth.resolvers :as auth.resolvers]
   [maintraq.db.models.facility :as facility]
   [maintraq.handlers.errors :as errors]
   [maintraq.validation.schema :as validation.schema]
   [struct.core :as st]))


(defn ^{:authorized? auth.resolvers/authenticated?}
  enumerate
  "List all known facilities."
  [{:keys [conn] :as ctx} _ _]
  (->> (d/q '[:find [?e ...]
              :in $
              :where
              [?e :facility/uid _]]
            (d/db conn))
       (map #(d/entity (d/db conn) %))))


(defn ^{:authorized? auth.resolvers/authenticated?}
  retrieve
  "Retrieve a facility by name."
  [{:keys [conn] :as ctx} {{:keys [name] :as input} :input :as args} _]
  (if-some [facility (d/entity (d/db conn) [:facility/name name])]
    facility
    (errors/not-found "Facility not found.")))


(defn ^{:authorized? auth.resolvers/admin?}
  create!
  "Create a facility."
  [{:keys [conn] :as ctx} {{:keys [name] :as input} :input :as args} _]
  (let [[errors] (st/validate input (validation.schema/facility (d/db conn)))]
    (if (some? errors)
      (errors/bad-request "Validation error" errors)
      (let [facility                   (facility/create input)
            {:keys [db-after tempids]} @(d/transact conn [facility])]
        (d/entity
         db-after
         (d/resolve-tempid db-after tempids (:db/id facility)))))))


(def resolvers
  {;; Queries
   :facilities/enumerate enumerate
   :facilities/retrieve  retrieve

   ;; Mutations
   :facilities/create! create!})
