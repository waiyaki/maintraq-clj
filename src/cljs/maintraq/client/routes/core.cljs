(ns maintraq.client.routes.core
  (:require
   [re-frame.core :as rf]
   [reitit.coercion.spec :as rc.spec]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfe.easy]
   [reitit.frontend.controllers :as rfe.controllers]
   [maintraq.client.routes.paths :as paths]
   [maintraq.client.home.views :as home.views]
   [maintraq.client.auth.views :as auth.views]))


(def debug? ^boolean goog.DEBUG)

;; Events

(rf/reg-fx
 ::navigate!
 (fn [route]
   (apply rfe.easy/push-state route)))


;; Effects

(rf/reg-event-fx
 ::navigate
 (fn [cofx [_ & route]]
   {::navigate! route}))


(rf/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfe.controllers/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))


;; Subscriptions

(rf/reg-sub
 ::current-route
 (fn [db]
   (:current-route db)))


(defn on-navigate
  "Handler to call whenever the route changes. This can be used to apply some
  controllers to execute some logic when the route changes to a certain route."
  [new-match]
  (when new-match
    (rf/dispatch [::navigated new-match])))


(def routes
  ["/"
   [""
    {:name paths/index
     :view home.views/page}]
   ["login"
    {:name paths/login
     :view auth.views/login}]
   ["sign-up"
    {:name paths/signup
     :view auth.views/signup}]])


(def router
  (rfe/router
   routes
   {:data {:coercion rc.spec/coercion}}))


(defn router-component [{:keys [router]}]
  (let [current-route @(rf/subscribe [::current-route])]
    (when current-route
      [(-> current-route :data :view)])))


(defn init-routes! []
  (when debug?
    (println "Initializing routes..."))
  (rfe.easy/start! router
                   on-navigate
                   {:use-fragment false}))
