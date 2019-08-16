(ns maintraq.env
  (:require
   [ring.middleware.reload :refer [wrap-reload]]))


(defn wrap-env [handler]
  (-> handler
    wrap-reload))
