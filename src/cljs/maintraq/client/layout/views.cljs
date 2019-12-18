(ns maintraq.client.layout.views
  (:require
   [reitit.frontend.easy :as rfe.easy]
   [maintraq.client.routes.paths :as paths]))


(defn header []
  [:nav.navbar.is-fixed-top.is-light
   {:aria-label "main navigation", :role :navigation}
   [:div.navbar-brand
    [:a.navbar-item
     {:href (rfe.easy/href paths/index)}
     [:figure.image.is-48x48
      [:img
       {:src   "./img/logo.png"
        :style {:width      "100%"
                :height     "100%"
                :max-height :initial}}]]]
    [:a.navbar-burger.burger
     {:data-target   :header-navbar,
      :aria-expanded false,
      :aria-label    :menu
      :role          :button}
     [:span {:aria-hidden true}]
     [:span {:aria-hidden true}]
     [:span {:aria-hidden true}]]]
   [:div#header-navbar.navbar-menu
    [:div.navbar-end
     [:div.navbar-item
      [:div.buttons
       [:a.button.is-dark.is-outlined {:href (rfe.easy/href paths/login)} "Log in"]
       [:a.button.is-dark.is-outlined {:href (rfe.easy/href paths/signup)} [:strong "Sign up"]]]]]]])


(defn app-container [& [children]]
  [:<>
   [header]
   [:div.container.is-fluid
    {:style {:margin-top "4rem"}}
    children]])
