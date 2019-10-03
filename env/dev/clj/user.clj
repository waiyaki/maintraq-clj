(ns user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :refer [refresh]]
   [datomic.api :as d]
   [expound.alpha :as expound]
   [maintraq.core :refer [start-app]]
   [maintraq.db.core :refer [conn]]
   [maintraq.seed.core :as seed]
   [mount.core :as mount]
   [taoensso.timbre :as timbre]))


(defn start []
  (start-app ["-e" "dev"])
  (seed/seed! conn)
  :ready)


(def stop mount/stop)


(defn go! []
  (set! s/*explain-out* expound/printer)
  (start))


(defn reset []
  (stop)
  (refresh :after 'user/go!))


(comment
  (def test-user (d/entity (d/db conn) [:user/username "waiyaki"]))

  (defn activate-test-user
    ([] (activate-test-user test-user))
    ([t-user]
     @(d/transact conn [{:db/id (:db/id t-user)
                         :user/activated true}])
     (d/entity (d/db conn) (:db/id t-user))))

  (activate-test-user)

)
