(ns maintraq.client.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [maintraq.client.events :as events]
   [maintraq.client.routes.core :as routes]
   [maintraq.client.layout.views :as layout.views]))


(defn app []
  [layout.views/app-container
   (routes/router-component {:router routes/router})])


(defn mount-root []
  (rf/clear-subscription-cache!)
  (routes/init-routes!)
  (reagent/render [app]
                  (.getElementById js/document "app")))


(defn ^:export init! []
  (rf/dispatch-sync [::events/initialize-db])
  (mount-root))
