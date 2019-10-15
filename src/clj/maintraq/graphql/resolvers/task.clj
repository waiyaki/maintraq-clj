(ns maintraq.graphql.resolvers.task
  (:require
   [datomic.api :as d]
   [maintraq.auth.resolvers :as auth.resolvers]
   [maintraq.db.models.audit :as audit]))


(defn ^{:authorized? (constantly true)}
  status
  "Extract the status of a task from a task entity."
  [_ _ task]
  (some->> task :task/status name))


(defn ^{:authorized? auth.resolvers/authenticated?}
  enumerate
  "List all tasks."
  [{:keys [conn] :as ctx} _ _]
  (->> (d/q '[:find [?e ...]
              :in $
              :where
              [?e :task/uid _]]
            (d/db conn))
       (map #(d/entity (d/db conn) %))))


(def resolvers
  {;; Queries
   :tasks/enumerate enumerate
   :tasks/status    status})
