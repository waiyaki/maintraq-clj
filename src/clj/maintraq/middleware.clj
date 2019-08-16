(ns maintraq.middleware
  (:require
   [muuntaja.core :as m]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [reitit.ring.middleware.exception :as exception]
   [maintraq.env :as env]))


(def exception-middleware
  (exception/create-exception-middleware exception/default-handlers))


(def formats
  (m/create m/default-options))


(defn wrap-base [handler]
  (-> handler
    env/wrap-env
    (wrap-defaults api-defaults)))
