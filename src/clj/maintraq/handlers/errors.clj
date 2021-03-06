(ns maintraq.handlers.errors
  (:require
   [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
   [maintraq.utils.core :as ut]))


(defn not-found [message]
  (resolve-as nil {:status 404
                   :message message}))


(defn bad-request
  ([message]
   (bad-request message nil))
  ([message errors]
   (resolve-as nil (ut/remove-nils {:status  400
                                    :message message
                                    :errors  errors}))))


(defn conflict [message]
  (resolve-as nil {:status  409
                   :message message}))


(defn unauthorized
  ([]
   (unauthorized "Invalid authentication credentials."))
  ([message]
   (resolve-as nil {:status  401
                    :message message})))


(defn forbidden
  ([]
   (forbidden "Forbidden."))
  ([message]
   (resolve-as nil {:status  403
                    :message message})))
