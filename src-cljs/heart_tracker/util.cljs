(ns heart-tracker.util
  (:require [cognitect.transit :as t]
            [om.next :as om]
            [goog.i18n.DateTimeFormat :as dtf]
            [goog.string :as gstring]
            [goog.string.format]
            [heart-tracker.state :refer [app-state]])
  (:import [goog.net XhrIo]
           [goog.i18n NumberFormat]))

(defrecord BpResult [id systolic diastolic heart-rate date-taken])

(defn transit-post [url]
  (fn [{:keys [remote] :as ast} cb]
    (let [{:keys [query rewrite] :as ast} (om/process-roots remote)]
      (.send XhrIo url
             (fn [e]
               (this-as this
                 (cb (rewrite (t/read (om/reader {:handlers {"bpResult" (fn [bp-result]
                                                                          {:db/id (aget bp-result 0)
                                                                           :user/systolic (aget bp-result 1)
                                                                           :user/diastolic (aget bp-result 2)
                                                                           :user/heartRate (aget bp-result 3)
                                                                           :user/dateTaken (aget bp-result 4)})}})
                                      (.getResponseText this))))))
             "POST" (t/write (om/writer) query)
             (clj->js {"Content-Type" "application/transit+json"
                       "Authorize" (-> @app-state :current/user :user/emailAddress)})))))