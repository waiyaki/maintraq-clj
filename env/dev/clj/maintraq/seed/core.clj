(ns maintraq.seed.core
  (:require
   [maintraq.seed.users :as seed.users]
   [io.rkn.conformity :as conformity]
   [taoensso.timbre :as timbre]))


(defn norms
  "Migration norms for seed data."
  []
  {::add-users
   {:txes [(conj (repeatedly 10 seed.users/user)
                 (seed.users/admin))]}})


(defn seed! [conn]
  (timbre/info "Seeding database...")
  (conformity/ensure-conforms conn (norms)))
