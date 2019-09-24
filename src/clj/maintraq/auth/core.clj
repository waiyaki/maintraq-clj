(ns maintraq.auth.core
  (:require
   [buddy.sign.jwt :as jwt]
   [maintraq.config :as config :refer [config]]
   [tick.alpha.api :as t]))


(defn sign
  [data]
  (jwt/sign
   {:data data
    :exp (t/inst (t/+ (t/now)
                     (t/new-duration 24 :hours)))}
   (config/auth-secret config)))


(defn unsign
  [token]
  (jwt/unsign token
              (config/auth-secret config)
              {:now (t/inst (t/now))}))
