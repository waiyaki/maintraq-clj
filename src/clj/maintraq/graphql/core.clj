(ns maintraq.graphql.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [mount.core :as mount :refer [defstate]]
   [maintraq.graphql.resolvers.user :as user]))


(defstate compiled-schema
  :start (-> "graphql/schema.edn"
             io/resource
             slurp
             edn/read-string
             (util/attach-resolvers
              (merge {:get (fn [& ks]
                             (fn [_ _ v]
                               (get-in v ks)))}
                     user/resolvers))
             schema/compile))


(defn execute [schema query-string variables context]
  (lacinia/execute schema query-string variables context))


(defn handler [req]
  {:status 200
   :body (execute compiled-schema (-> req :body slurp) nil nil)})
