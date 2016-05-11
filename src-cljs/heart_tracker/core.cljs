(ns heart-tracker.core
  (:require [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [sablono.core :refer-macros [html]]
            [heart-tracker.reconciler :refer [reconciler]]
            [heart-tracker.ui.elements :as el]))

(enable-console-print!)

(defui BpResults
  static om/Ident
  (ident [_ {:keys [db/id]}]
    [:bpResults/by-id id])
  static om/IQuery
  (query [_]
    [:db/id :user/heartRate :user/systolic :user/diastolic :user/dateTaken])
  Object
  (render [this]
    (let [props (om/props this)]
      (println "Props -> " props)
      (html
        [:.conatainer-fluid
         [:.row
          [:.col-lg-12
           [:h1.page-header "Heart Information"]]]
         [:.row
          [:.col-sm-4
           (el/bp-form this props)]]
         [:.row
          [:.col-lg-6
           [:h2 "Results"]
           (el/bp-table this props)]]]))))

(defui Dashboard
  Object
  (render [this]
    (html [:h1 "Dashboard"])))

(def page
  {:dashboard  Dashboard
   :heart-info BpResults})

(def page->component
  (zipmap (keys page)
          (map om/factory (vals page))))

(def page->functions
  {:heart-info
   {:add-info (fn [c systolic diastolic heart-rate])}})

(defui RootView
  static om/IQuery
  (query [_]
    [:app/page
     {:current/user
      [:user/emailAddress :user/apiKey
       {:user/bpResults (om/get-query BpResults)}]}])
  Object
  (initLocalState [_]
    {:user-menu-open? false})
  (render [this]
    (let [props (om/props this)
          active-component (page->component (:app/page props))]
      (html
        [:div#wrapper
         (el/navbar this props)
         [:div#page-wrapper
          (active-component props)]]))))




(om/add-root! reconciler RootView (gdom/getElement "app"))



