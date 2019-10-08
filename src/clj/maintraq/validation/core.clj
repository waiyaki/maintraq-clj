(ns maintraq.validation.core
  (:require
   [datomic.api :as d]))


(defn unique [db attr]
  {:message "is unavailable"
   :optional true
   :state true
   :validate (fn [state v]
               (nil? (d/q '[:find ?e .
                            :in $ ?attr ?v
                            :where
                            [?e ?attr ?v]]
                          db attr v)))})
