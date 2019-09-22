(ns maintraq.config
  (:require
   [clojure.java.io :as io]
   [aero.core :as aero]
   [mount.core :as mount :refer [defstate]]))


(defn- read-config [& [env]]
  (aero/read-config
   (io/resource "config/config.edn")
   {:resolver aero/root-resolver
    :profile (or env (:env (mount/args)))}))


(defstate config
  :start (read-config))


(defn datomic-uri [config]
  (get-in config [:datomic :uri]))


(defn server-port [config]
  (get-in config [:server :port]))


(defn host [config app-name]
  (get-in config [:hosts app-name]))


(defn mailgun-api-key [config]
  (get-in config [:secrets :services :mailgun :api-key]))


(defn mailgun-domain [config]
  (get-in config [:secrets :services :mailgun :domain]))


(defn auth-secret [config]
  (get-in config [:secrets :auth :maintraq]))
