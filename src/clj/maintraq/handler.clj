(ns maintraq.handler
  (:require
   [clojure.java.io :as io]
   [mount.core :as mount :refer [defstate]]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [maintraq.middleware :as middleware]
   [maintraq.graphql.core :as graphql]))


(defn hello [request]
  {:status 200
   :body   {:message "Hello, world!"}})


(defn graphiql [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (slurp (io/resource "public/graphiql.html"))})


(defstate app-routes
  :start (ring/ring-handler
          (ring/router
           [["/graphiql" {:get {:handler graphiql}}]
            ["/api"
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
             ["" {:get {:handler hello}}]
             ["/graphql" {:post {:handler graphql/handler}}]]])))


(defn app []
  (middleware/wrap-base #'app-routes))
