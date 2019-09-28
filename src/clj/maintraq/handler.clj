(ns maintraq.handler
  (:require
   [maintraq.handlers.middleware :as middleware]
   [maintraq.handlers.routes :as routes]))


(defn app [deps]
  (-> #'routes/app-routes
      middleware/wrap-base
      (middleware/wrap-deps deps)))
