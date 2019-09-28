(ns maintraq.deps
  (:require
   [maintraq.db.core :as db :refer [conn]]))


(defn deps []
  {:conn conn})
