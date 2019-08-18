(ns maintraq.handler
  (:require
   [mount.core :as mount :refer [defstate]]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [maintraq.middleware :as middleware]))


(defn hello [request]
  {:status 200
   :body   {:message "Hello, world!"}})


(defstate app-routes
  :start (ring/ring-handler
           (ring/router
             [["/api"
               {:muuntaja   middleware/formats
                :middleware [;; query-params & form-params
                             parameters/parameters-middleware
                             ;; content-negotiation
                             muuntaja/format-negotiate-middleware
                             ;; encoding response body
                             muuntaja/format-response-middleware
                             ;; exception handling
                             middleware/exception-middleware
                             ;; decoding request body
                             muuntaja/format-request-middleware
                             ;; coercing response bodys
                             coercion/coerce-response-middleware
                             ;; coercing request parameters
                             coercion/coerce-request-middleware]}
               ["" {:get {:handler hello}}]]])))


(defn app []
  (middleware/wrap-base #'app-routes))
