(ns maintraq.handler
  (:require
   [maintraq.handlers.middleware :as middleware]
   [maintraq.handlers.routes :as routes]))


(defn app []
  (middleware/wrap-base #'routes/app-routes))
