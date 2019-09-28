(ns user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :refer [refresh]]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [maintraq.core :refer [start-app]]
   [taoensso.timbre :as timbre]))


(defn start []
  (start-app ["-e" "dev"]))


(def stop mount/stop)


(defn go! []
  (set! s/*explain-out* expound/printer)
  (start))


(defn reset []
  (stop)
  (refresh :after 'user/go!))
