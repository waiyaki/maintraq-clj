(ns maintraq.graphql.resolvers.facility
  (:require
   [datomic.api :as d]
   [maintraq.auth.resolvers :as auth.resolvers]
   [maintraq.handlers.errors :as errors]))


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


(defn ^{:authorized? auth.resolvers/authenticated?} retrieve
  "Retrieve a facility by name."
  [{:keys [conn] :as ctx} {{:keys [name] :as input} :input :as args} _]
  (if-some [facility (d/entity (d/db conn) [:facility/name name])]
    facility
    (errors/not-found "Facility not found.")))


(def resolvers
  {;; Queries
   :facilities/enumerate enumerate
   :facilities/retrieve  retrieve})
