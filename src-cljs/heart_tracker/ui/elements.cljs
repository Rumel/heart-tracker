(ns heart-tracker.ui.elements
  (:require [sablono.core :refer-macros [html]]
            [om.next :as om]
            cljsjs.react-datepicker)
  (:import (goog.date DateTime)))


(defn format-date
  [d]
  (.format (js/moment d) "MM/DD/YYYY"))

(defmulti user-menu (fn [_ props]
                      (if (contains? props :current/user)
                        :logged-in
                        :logged-out)))

(defn change
  [c k e]
  (om/update-state! c assoc k (.. e -target -value)))

(defn clear-fields
  [c]
  (om/update-state! c
                    (fn [st]
                      (-> st
                          (assoc :systolic "")
                          (assoc :diastolic "")
                          (assoc :heartRate "")
                          (assoc :user/dateTaken "")))))

(defn date-picker [props]
  (.createElement js/React js/DatePicker (clj->js props)))

(defn date-change [c]
  (fn [date]
    (om/update-state! c assoc :dateTaken date)))

(defn bp-form
  [c pros]
  (let [{:keys [systolic diastolic heartRate dateTaken]} (om/get-state c)
        {:keys [add-info]} (om/get-computed c)]
    (html
      [:.panel.panel-primary
       [:.panel-heading
        [:h3.panel-title "Add Heart Info"]]
       [:.panel-body
        [:form
         [:.form-group
          [:label "Systolic"]
          [:input.form-control {:value       systolic
                                :on-change   #(change c :systolic %)
                                :placeholder "Systolic"}]]
         [:.form-group
          [:label "Diastolic"]
          [:input.form-control {:value       diastolic
                                :on-change   #(change c :diastolic %)
                                :placeholder "Diastolic"}]]
         [:.form-group
          [:label "Heart Rate"]
          [:input.form-control {:value       heartRate
                                :on-change   #(change c :heartRate %)
                                :placeholder "Heart Rate"}]]
         [:.form-group
          [:label "Date Taken"]
          (date-picker {:className "form-control"
                        :selected  (om/get-state c :dateTaken)
                        :onChange  (date-change c)})]]]
       [:a
        {:href     "#"
         :on-click #(do
                     (.preventDefault %)
                     (add-info (js/parseInt systolic)
                               (js/parseInt diastolic)
                               (js/parseInt heartRate)
                               (.toDate dateTaken))
                     (clear-fields c))}
        [:.panel-footer
         [:span.pull-left "Add Record"]
         [:span.pull-right
          [:i.fa.fa-arrow-circle-right]]
         [:.clearfix]]]])))


(defn bp-result
  [{:keys [db/id user/systolic user/diastolic user/dateTaken user/heartRate] :as props}]
  (let [style (cond-> {:key id}
                      (> systolic 130) (merge {:class "warning"})
                      (> systolic 140) (merge {:class "danger"}))]

    (html
      [:tr style
       [:td (format-date dateTaken)]
       [:td systolic]
       [:td diastolic]
       [:td heartRate]
       [:td]])))


(defn bp-table
  [c props]
  (let [{:keys [user/bpResults]} (get props :current/user)]
    (println "bpResults: " bpResults)
    (html
      [:.table-responsive
       [:table.table.table-bordered.table-hover.table-striped
        [:thead
         [:tr
          [:th "Date"]
          [:th "Systolic"]
          [:th "Diastolic"]
          [:th "Heart Rate"]
          [:th]]]
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
