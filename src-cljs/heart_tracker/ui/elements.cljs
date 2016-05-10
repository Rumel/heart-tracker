(ns heart-tracker.ui.elements
  (:require [sablono.core :refer-macros [html]]
            [om.next :as om]))


(defmulti user-menu (fn [_ props]
                      (if (contains? props :current/user)
                        :logged-in
                        :logged-out)))

(defn navbar
  [c props]
  (html
    [:nav.navbar.navbar-inverse.navbar-fixed-top
     {:role "navigation"}
     [:.navbar-header
      [:button {:type "button"
                :class "navbar-toggle"}
       [:span.sr-only "Toggle Navigation"]
       [:span.icon-bar]
       [:span.icon-bar]
       [:span.icon-bar]]
      [:a.navbar-brand
       {:href "/"} "Heart Tracker"]]
     [:.nav.navbar-right.top-nav
      (user-menu c props)]]))

(defmethod user-menu :logged-in
  [c {:keys [current/user]}]
  (let [{:keys [user-menu-open?]} (om/get-state c)]
    (html
      [:li (cond-> {:class "dropdown"}
                   (true? user-menu-open?) (merge {:class "dropdown open"}))
       [:a.dropdown-toggle {:href "#"
                            :on-click #(om/update-state! c
                                                         assoc :user-menu-open? true)}
        (:user/emailAddress user)]
       [:ul.dropdown-menu
        [:li
         [:a {:href "#"} [:i.fa.fa-fw.fa-gear] "Add Result"]]]])))


(defmethod user-menu :logged-out
  [c props]
  (html
    [:.test]))
