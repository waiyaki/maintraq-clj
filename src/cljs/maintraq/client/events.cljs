(ns maintraq.client.events
  (:require
   [re-frame.core :as rf]))


(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:current-route nil}))
