(ns maintraq.test-utils.graphql.core
  (:require
   [clj-http.client :as client]
   [jsonista.core :as json]
   [maintraq.config :as config :refer [config]]
   [venia.core :as v]))


(defn mutation!
  ([q] (mutation! q {}))
  ([q {:keys [headers]
       :or   {headers {}}}]
   (try
     (client/post (format "%s/api/graphql" (config/host config :api))
                  {:accept       :application/json
                   :content-type :application/json
                   :as           :json
                   :form-params  {:query q}
                   :headers      headers})
     (catch Exception e
       (update (ex-data e) :body #(json/read-value
                                   %
                                   (json/object-mapper {:decode-key-fn true})))))))


(defn mutation [{:keys [queries name]
                 :or   {name (clojure.core/name (ffirst queries))}}
                & [{:as opts}]]
  (mutation!
   (v/graphql-query {:venia/operation {:operation/type :mutation
                                       :operation/name name}
                     :venia/queries   queries})
   opts))


(defn query [{:keys [queries name]
              :or   {name (clojure.core/name (ffirst queries))}}
             & [{:as opts}]]
  (mutation!
   (v/graphql-query {:venia/operation {:operation/type :query
                                       :operation/name name}
                     :venia/queries   queries})
   opts))
