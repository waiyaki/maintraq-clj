(ns maintraq.client.core
  (:require
   [reagent.core :as r]
   [maintraq.client.layout.views :as layout.views]))


(defn app []
  [layout.views/home-page])


(defn mount-root []
  (r/render [app] (.getElementById js/document "app")))


(defn ^:export init! []
  (mount-root))
