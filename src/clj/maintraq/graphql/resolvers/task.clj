(ns maintraq.graphql.resolvers.task
  (:require
   [datomic.api :as d]
   [maintraq.auth.resolvers :as auth.resolvers]
   [maintraq.db.models.audit :as audit]
   [maintraq.db.models.task :as task]
   [maintraq.db.models.user :as user]
   [maintraq.handlers.errors :as errors]
   [maintraq.utils.core :as ut]
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
  (let [[errors] (st/validate input (validation.schema/task-create))]
    (if (some? errors)
      (errors/bad-request "Validation error" errors)
      (let [task                       (task/create (merge input {:requester requester}))
            audit                      (audit/create requester)
            {:keys [db-after tempids]} @(d/transact conn [task audit])]
        (d/entity
         db-after
         (d/resolve-tempid db-after tempids (:db/id task)))))))


(defn ^{:authorized? auth.resolvers/authenticated?}
  retrieve
  "Retrieve a single facility by its unique id."
  [{:keys [conn] :as ctx} {{:keys [uid] :as input} :input :as args} _]
  (if-some [task (d/entity (d/db conn) [:task/uid uid])]
    task
    (errors/not-found "Task not found.")))


(defn- task-requester?
  "Users can update tasks they have requested."
  [{:keys [db requester]} {{:keys [uid]} :input} _]
  (let [task (d/entity db [:task/uid uid])]
    (= (:db/id requester)
       (:db/id (:task/requester task)))))


(defn- member-update [task {:keys [title description] :as args}]
  (if (not= (set (keys args)) #{:title :description})
    (Exception. "Members can only update a task's title or description.")
    (task/update-data task args)))


(defn- maintenance-update [task args]
  (if-some [assignee (:assignee args)]
    (Exception. "Maintenance crew members cannot change a task's assignee.")
    (task/update-data task args)))


(defn- admin-update [task args]
  (if (and (some? (:assignee args))
           (not (user/maintenance? (:assignee args))))
    (Exception. "Invalid assignee. Tasks can be assigned to maintenance users only.")
    (task/update-data task args)))


(defn- update-data [requester task args]
  (case (:user/role requester)
    :user.role/admin       (admin-update task args)
    :user.role/maintenance (maintenance-update task args)
    :user.role/member      (member-update task args)))


(defn ^{:authorized? [auth.resolvers/admin? auth.resolvers/maintenance? task-requester?]}
  update!
  "Update a task."
  [{:keys [conn requester] :as ctx} {{:keys [uid params] :as input} :input :as args} _]
  (let [[errors] (st/validate params (validation.schema/task-update))
        task     (d/entity (d/db conn) [:task/uid uid])
        assignee (when (:assignee params) (d/entity (d/db conn) [:user/uid (:assignee params)]))
        params   (ut/remove-nils (assoc params :assignee assignee))
        tx-task  (update-data requester task params)
        tx-audit (audit/create requester)]
    (cond
      (some? errors)                (errors/bad-request "Validation error" errors)
      (nil? task)                   (errors/not-found "Task not found.")
      (instance? Exception tx-task) (errors/bad-request (ex-message tx-task))
      :else
      (let [{:keys [db-after]} @(d/transact conn [tx-task tx-audit])]
        (d/entity db-after (:db/id task))))))


(def resolvers
  {;; Queries
   :tasks/enumerate enumerate
   :tasks/status    status
   :tasks/retrieve  retrieve

   ;; Mutations
   :tasks/create! create!
   :tasks/update! update!})
