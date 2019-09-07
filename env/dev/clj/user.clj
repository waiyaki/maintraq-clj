(ns user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :refer [refresh]]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [maintraq.core :refer [start-app]]
   [maintraq.db.core :refer [conn]]
   [maintraq.seed.core :as seed]
   [taoensso.timbre :as timbre]))


(defn start []
  (let [started (:started (mount/start-with-args {:env :dev}))]
    (seed/seed! conn)
    (doseq [component started]
      (timbre/info "started" component))))


(def stop mount/stop)


(defn go! []
  (set! s/*explain-out* expound/printer)
  (start))


(defn reset []
  (stop)
  (refresh :after 'user/go!))
