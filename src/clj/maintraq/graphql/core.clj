(ns maintraq.graphql.core
  (:require
   [buddy.auth :refer [authenticated?]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [mount.core :as mount :refer [defstate]]
   [maintraq.auth.resolvers :as auth.resolvers]
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


(def utility-resolvers
  {:get (fn [& ks]
          (fn [_ _ v]
            (get-in v ks)))})


(def unauthenticated-schema-files
  ["unauthenticated_mutations.edn" "unauthenticated_queries.edn"])


(def authenticated-schema-files
  (concat unauthenticated-schema-files ["mutations.edn" "queries.edn"]))


(def resolvers
  (apply (partial merge utility-resolvers)
         (apply auth.resolvers/authorize-resolvers
                [#'user/resolvers])))


(defn- make-entire-schema [schema-files]
  (->> (concat schema-files  ["objects.edn" "input_objects.edn" "enums.edn"])
       (map #(edn/read-string (slurp (io/resource (str "graphql/" %)))))
       (apply merge-with merge custom-scalars)))


(defn- compile-schema [schema-files]
  (-> schema-files
      make-entire-schema
      (util/attach-resolvers resolvers)
      schema/compile))


(defstate unauthenticated-schema
  :start (compile-schema unauthenticated-schema-files))


(defstate authenticated-schema
  :start (compile-schema authenticated-schema-files))


(defn ->context [req]
  {:deps      (:deps req)
   :conn      (-> req :deps :conn)
   :requester (:identity req)})


(defn execute [schema query-string variables context options]
  (lacinia/execute schema query-string variables context options))


(defn handler
  "Handle a GraphQL request.

  When the response returned by the resolver has errors, the response status
  is attempted to be obtained from the extensions of the first error message.
  For errors without a status extension, a generic 500 response status is used."
  [req]
  (let [{:keys [query variables operationName]} (:body-params req)
        {:keys [errors] :as res}
        (execute (if (authenticated? req)
                   authenticated-schema
                   unauthenticated-schema)
                 query
                 variables
                 (->context req)
                 {:operation-name operationName})

        status (if-not (some? errors)
                 200
                 (or (some-> errors first :extensions :status) 500))]
    {:status status
     :body   res}))
