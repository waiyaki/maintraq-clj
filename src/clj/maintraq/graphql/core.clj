(ns maintraq.graphql.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [mount.core :as mount :refer [defstate]]
   [maintraq.graphql.resolvers.user :as user]))


(defn parse-scalar
  "Wrap a custom scalar parsing function in a try-catch, returning nil in case
  of an error in accordance to Lacinia's flow"
  [f]
  (fn [v]
    (try
      (f v)
      (catch Throwable _
        nil))))


(def custom-scalars
  {:scalars
   {:Long {:parse     (parse-scalar #(Long. %))
           :serialize (parse-scalar #(Long. %))}

    :UUID {:parse     (parse-scalar #(java.util.UUID/fromString %))
           :serialize (parse-scalar str)}}})


(defstate compiled-schema
  :start (-> "graphql/schema.edn"
             io/resource
             slurp
             edn/read-string
             (merge custom-scalars)
             (util/attach-resolvers
              (merge {:get (fn [& ks]
                             (fn [_ _ v]
                               (get-in v ks)))}
                     user/resolvers))
             schema/compile))


(defn ->context [req]
  {:deps (:deps req)
   :conn (-> req :deps :conn)})


(defn execute [schema query-string variables context options]
  (lacinia/execute schema query-string variables context options))


(defn handler [req]
  (let [{:keys [query
                variables
                operationName]}  (:body-params req)
        {:keys [errors] :as res} (execute compiled-schema
                                          query
                                          variables
                                          (->context req)
                                          {:operation-name operationName})
        status                   (or (some-> errors first :extensions :status) 200)]
    {:status status
     :body   res}))
