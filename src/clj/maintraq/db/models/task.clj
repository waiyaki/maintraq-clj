(ns maintraq.db.models.task
  (:require
   [datomic.api :as d]
   [maintraq.db.partition :as db.partition]))


(defn create
  "Return a map representing a task."
  [{:keys [title description requester] :as opts}]
  {:db/id            (d/tempid db.partition/tasks)
   :task/title       title
   :task/description description
   :task/uid         (d/squuid)
   :task/requester   (:db/id requester)
   :task/status      :task.status/initial})
