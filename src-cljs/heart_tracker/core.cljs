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
     [:db/id :user/heartRate :user/systolic :user/diastolic :user/dateTaken]))

 (defui RootView
   static om/IQuery
   (query [_]
     [{:current/user
       [:user/emailAddress :user/apiKey
        {:user/bpResults (om/get-query BpResults)}]}])
   Object
   (initLocalState [_]
     {:user-menu-open? false})
   (render [this]
     (let [props (om/props this)]
       (html
         [:.wrapper
          (el/navbar this props)]))))


(om/add-root! reconciler RootView (gdom/getElement "app"))



