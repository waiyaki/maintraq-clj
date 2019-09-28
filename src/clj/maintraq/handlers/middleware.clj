(ns maintraq.handlers.middleware
  (:require
   [muuntaja.core :as m]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.logger :as logger]
   [reitit.ring.middleware.exception :as exception]
   [maintraq.env :as env]
   [taoensso.timbre :as timbre]))


(def exception-middleware
  (exception/create-exception-middleware
   (merge exception/default-handlers
          {::exception/default
           (fn [^Exception e _]
             (timbre/error e ::error)
             {:status 500
              :body {:type "Server Error"
                     :message "A server error occurred."}})})))


(def formats
  (m/create m/default-options))


(defn wrap-deps [handler deps]
  (fn [req]
    (handler (assoc req :deps deps))))


(defn wrap-base [handler]
  (-> handler
      (logger/wrap-with-logger {:log-fn (fn [{:keys [level throwable message]}]
                                          (timbre/log level throwable message))})
      env/wrap-env
      (wrap-defaults api-defaults)))
