(ns maintraq.auth.middleware
  (:require
   [buddy.auth.backends :as backends]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [datomic.api :as d]
   [maintraq.auth.core :as auth]
   [maintraq.config :as config :refer [config]]))


(defn- token-authfn [req token]
  (let [{{:keys [uid]} :data} (auth/unsign token)]
    (d/entity (d/db (-> req :deps :conn)) [:user/uid (java.util.UUID/fromString uid)])))


(def backend (backends/token {:authfn     token-authfn
                              :token-name "Bearer"}))


(defn wrap-auth [handler]
  (-> handler
      (wrap-authentication backend)))
