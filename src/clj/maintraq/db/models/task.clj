(ns maintraq.db.models.task
  (:require
   [datomic.api :as d]
   [maintraq.db.partition :as db.partition]
   [maintraq.utils.core :as ut]))


(defn create
  "Return a map representing a task."
  [{:keys [title description requester] :as opts}]
  {:db/id            (d/tempid db.partition/tasks)
   :task/title       title
   :task/description description
   :task/uid         (d/squuid)
   :task/requester   (:db/id requester)
   :task/status      :task.status/initial})


(defn- status->task-status [status]
  (when-some [status status]
    (case status
      :initial      :task.status/initial
      :confirmed    :task.status/confirmed
      :acknowledged :task.status/acknowledged
      :started      :task.status/started
      :pending      :task.status/pending
      :done         :task.status/done)))


(defn update-data
  "Return a map of data which the given task should be updated to."
  [task {:keys [title description assignee status]}]
  (ut/remove-nils
   {:db/id            (:db/id task)
    :task/title       title
    :task/description description
    :task/assignee    (when assignee (:db/id assignee))
    :task/status      (status->task-status status)}))
