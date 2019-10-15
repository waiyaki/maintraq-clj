(ns maintraq.seed.core
  (:require
   [maintraq.seed.facilities :as seed.facilities]
   [maintraq.seed.tasks :as seed.tasks]
   [maintraq.seed.users :as seed.users]
   [io.rkn.conformity :as conformity]
   [taoensso.timbre :as timbre]))


(defn norms
  "Migration norms for seed data."
  []
  {::add-users
   {:txes [(conj (repeatedly 10 seed.users/user)
                 (seed.users/admin))]}
   ::add-facilities
   {:txes [(repeatedly 10 seed.facilities/facility)]}

   ::add-tasks
   {:txes [(repeatedly 10 seed.tasks/task)]}})


(defn seed! [conn]
  (timbre/info "Seeding database...")
  (conformity/ensure-conforms conn (norms)))
