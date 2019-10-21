(ns maintraq.graphql.resolvers.task
  (:require
   [datomic.api :as d]
   [maintraq.auth.resolvers :as auth.resolvers]
   [maintraq.db.models.audit :as audit]
   [maintraq.db.models.task :as task]
   [maintraq.handlers.errors :as errors]
   [maintraq.validation.schema :as validation.schema]
   [struct.core :as st]))


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


(defn ^{:authorized? auth.resolvers/authenticated?}
  create!
  "Create a task."
  [{:keys [conn requester] :as ctx} {:keys [input] :as args} _]
  (let [[errors] (st/validate input (validation.schema/task (d/db conn)))]
    (if (some? errors)
      (errors/bad-request "Validation error" errors)
      (let [task                       (task/create (merge input {:requester requester}))
            audit                      (audit/create requester)
            {:keys [db-after tempids]} @(d/transact conn [task audit])]
        (d/entity
         db-after
         (d/resolve-tempid db-after tempids (:db/id task)))))))


(def resolvers
  {;; Queries
   :tasks/enumerate enumerate
   :tasks/status    status

   ;; Mutations
   :tasks/create! create!})
