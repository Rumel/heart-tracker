(ns heart-tracker.ui.elements
  (:require [sablono.core :refer-macros [html]]
            [om.next :as om]))


(defmulti user-menu (fn [_ props]
                      (if (contains? props :current/user)
                        :logged-in
                        :logged-out)))

(defn bp-form
  [c props]
  (html
    [:.panel.panel-primary
     [:.panel-heading
      [:h3.panel-title "Add Heart Info"]]
     [:.panel-body
      [:form
       [:.form-group
        [:label "Systolic"]
        [:input.form-control {:placeholder "Systolic"}]]
       [:.form-group
        [:label "Diastolic"]
        [:input.form-control {:placeholder "Diastolic"}]]
       [:.form-group
        [:label "Heart Rate"]
        [:input.form-control {:placeholder "Heart Rate"}]]]]
     [:a {:href "#"}
      [:.panel-footer
       [:span.pull-left "Add Record"]
       [:span.pull-right
        [:i.fa.fa-arrow-circle-right]]
       [:.clearfix]]]]))

(defn bp-result
  [{:keys [user/systolic user/diastolic user/dateTaken user/heartRate]}]
  (let [style (cond-> {}
                      (> systolic 130) (merge {:class "warning"})
                      (> systolic 140) (merge {:class "danger"}))]

    (html
      [:tr style
       [:td]
       [:td systolic]
       [:td diastolic]
       [:td heartRate]])))

(defn bp-table
  [c props]
  (let [{:keys [user/bpResults]} (get props :current/user)]
    (html
      [:.table-responsive
       [:table.table.table-bordered.table-hover.table-striped
        [:thead
         [:tr
          [:th "Date"]
          [:th "Systolic"]
          [:th "Diastolic"]
          [:th "Heart Rate"]]]
        [:tbody
         (map bp-result bpResults)]]])))


(defn sidebar
  [c props]
  (when (:current/user props)
    (html
      [:.collapse.navbar-collapse.navbar-ex1-collapse
       [:ul.nav.navbar-nav.side-nav
        [:li (when (= :dashboard (:app/page props))
               {:class "active"})
         [:a {:href "#"
              :on-click (fn [e]
                          (.preventDefault e)
                          (om/transact! c '[(change/page {:page :dashboard}) :app/page]))}
          [:i.fa.fa-fw.fa-dashboard] " Dashboard"]]
        [:li (when (= :heart-info (:app/page props))
               {:class "active"})
         [:a {:href "#"
              :on-click (fn [e]
                          (.preventDefault e)
                          (om/transact! c '[(change/page {:page :heart-info}) :app/page]))}
          [:i.fa.fa-fw.fa-heart] " Heart Info"]]]])))

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
     [:ul.nav.navbar-right.top-nav
      (user-menu c props)]
     (sidebar c props)]))

(defmethod user-menu :logged-in
  [c {:keys [current/user]}]
  (let [{:keys [user-menu-open?]} (om/get-state c)]
    (html
      [:li (cond-> {:class "dropdown"}
                   (true? user-menu-open?) (merge {:class "dropdown open"}))
       [:a.dropdown-toggle {:href "#"
                            :on-click #(om/update-state! c
                                                         assoc :user-menu-open? true)}
        [:i.fa.fa-user] (str " " (:user/emailAddress user))]
       [:ul.dropdown-menu
        [:li
         [:a {:href "#"} [:i.fa.fa-fw.fa-gear] "Add Result"]]]])))


(defmethod user-menu :logged-out
  [c props]
  (html
    [:li.dropdown
     [:a.dropdown-toggle {:href "#"}] "Login"]))
