(ns maintraq.handlers.errors
  (:require
   [com.walmartlabs.lacinia.resolve :refer [resolve-as]]))


(defn not-found [message]
  (resolve-as nil {:status 404
                   :message message}))
