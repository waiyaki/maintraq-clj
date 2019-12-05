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
  (require '[maintraq.seed.users :as seed.users])

  (defn user [] (seed.users/user! conn (seed.users/user {:username "waiyaki"})))

  (defn activate-user [test-user]
    @(d/transact conn [[:db/add test-user :user/activated true]])
    @(d/transact conn [[:db/add test-user
                        :user/password (buddy.hashers/derive "password")]])
    (d/entity (d/db conn) test-user))

  (defn seed! []
    (user)
    (activate-user [:user/username "admin"])
    (activate-user [:user/username "waiyaki"]))

  (seed!)

)
