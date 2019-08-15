(ns maintraq.config
  (:require
   [clojure.java.io :as io]
   [aero.core :as aero]
   [mount.core :as mount :refer [defstate]]))


(defn- read-config [& [env]]
  (aero/read-config (io/resource "config/config.edn")
    {:resolver aero/root-resolver
     :profile (or env (:env (mount/args)))}))


(defstate config
  :start (read-config))


(defn datomic-uri [config]
  (get-in config [:datomic :uri]))
