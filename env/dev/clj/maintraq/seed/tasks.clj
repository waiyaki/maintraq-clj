(ns maintraq.seed.tasks
  (:require
   [datomic.api :as d]
   [faker.lorem]
   [maintraq.seed.users :as seed.users]
   [maintraq.db.partition :as db.partition]))


(defn task
  "Return a map representing a task."
  ([] (task {}))
  ([{:keys [uid title description status requester assignee] :as opts
     :or   {uid         (d/squuid)
            status      :task.status/initial
            title       (first (faker.lorem/sentences 6))
            description (first (faker.lorem/paragraphs))
            requester   (seed.users/user)
            assignee    (seed.users/user {:role :user.role/maintenance})}}]
   {:db/id            (db.partition/tempid db.partition/tasks)
    :task/uid         uid
    :task/title       title
    :task/description description
    :task/status      status
    :task/assignee    assignee
    :task/requester   requester}))


(defn task!
  "Seed a task into Datomic."
  ([conn] (task! (task)))
  ([conn task]
   (let [{:keys [db-after tempids]} @(d/transact conn [task])]
     (d/entity db-after (d/resolve-tempid db-after tempids (first (keys tempids)))))))
