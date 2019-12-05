(ns maintraq.client.layout.views)


(defn header []
  [:nav.navbar.is-fixed-top.is-light
   {:aria-label "main navigation", :role :navigation}
   [:div.navbar-brand
    [:a.navbar-item
     {:href "/"}
     [:figure.image.is-64x64
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
       [:a.button.is-dark.is-outlined "Log in"]
       [:a.button.is-dark.is-outlined [:strong "Sign up"]]]]]]])


(defn app-container []
  [:div.container.is-fluid
   {:style {:margin-top "5rem"}}
   [:h1.title.is-1 "Maintraq"]])


(defn home-page []
  [:<>
   [header]
   [app-container]])
