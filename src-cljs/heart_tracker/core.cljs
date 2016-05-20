(ns heart-tracker.core
  (:require [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [sablono.core :refer-macros [html]]
            [heart-tracker.reconciler :refer [reconciler]]
            [heart-tracker.ui.elements :as el]))

(enable-console-print!)

(defn chart-data
  [bpResults]
  (into []
        (map (fn [result]
               [(el/format-date (:user/dateTaken result)) (:user/heartRate result)]) bpResults)))

(defui BpResults
  static om/Ident
  (ident [_ {:keys [db/id]}]
    [:bpResults/by-id id])
  static om/IQuery
  (query [_]
    [:db/id :user/heartRate :user/systolic :user/diastolic :user/dateTaken])
  Object
  (initLocalState [this]
    {:systolic ""
     :diastolic ""
     :heartRate ""
     :dateTaken (js/moment.)})
  (render [this]
    (let [props (om/props this)
          {:keys [emailAddress]} (om/params this)]
      (html
        [:.conatainer-fluid
         [:.row
          [:.col-lg-12
           [:h1.page-header "Heart Information"]]]
         [:.row
          [:.col-sm-4
           (el/bp-form this props emailAddress)]
          [:.col-sm-8
           [:h2 "Results"]
           (el/bp-table this props)]]]))))

(defui Dashboard
  Object
  (initLocalState [_]
    {:flot nil})
  (componentDidMount [this]
    (let [data (some-> this
                   om/props
                   :current/user
                   :user/bpResults
                       chart-data)
          f (.plot js/jQuery (gdom/getElement "flot-line-chart")
                    (clj->js [data])
                   #js {:label "Heart Rate History"})]
      (om/update-state! this assoc :flot f)))
  (componentDidUpdate [this prevProps prevState]
    (let [props (om/props this)]))
  (render [this]
    (html [:.container-fluid
           [:.row
            [:col-lg-12
             [:h1.page-header "Dashboard"]]]
           [:.row
            [:.col-lg-12
             [:.panel.panel-primary
              [:.panel-heading
               [:h3.panel-title [:i.fa.fa-heart " Heart History"]]]
              [:.panel-body
               [:.flot-chart
                [:#flot-line-chart.flot-chart-content
                 {:style {:padding  "0px"
                          :position "relative"}}]]]]]]])))


(def page
  {:dashboard  Dashboard
   :heart-info BpResults})

(def page->component
  (zipmap (keys page)
          (map om/factory (vals page))))

(defn page->functions
  [c]
  {:heart-info
   {:add-info (fn [email systolic diastolic heart-rate date-taken]
                (om/transact! c `[(add/bpResult
                                    {:emailAddress ~email
                                     :systolic ~systolic
                                     :diastolic ~diastolic
                                     :heartRate ~heart-rate
                                     :dateTaken ~date-taken
                                     :db/id ~(om/tempid)})]))
    :delete-fn (fn [id]
                 (om/transact! c `[(remove/bpResult {:db/id ~id}) :current/user]))}})

(defui RootView
  static om/IQuery
  (query [_]
    `[:app/page
      {:current/user
          [:user/emailAddress
           {:user/bpResults ~(om/get-query BpResults)}]}])
  Object
  (initLocalState [_]
    {:user-menu-open? false})
  (render [this]
    (let [props (om/props this)
          page (:app/page props)
          active-component (page->component page)]
      (html
        [:div#wrapper
         (el/navbar this props)
         [:div#page-wrapper
          (active-component (om/computed
                              props
                              (get (page->functions this) page {})))]]))))

(om/add-root! reconciler RootView (gdom/getElement "app"))



