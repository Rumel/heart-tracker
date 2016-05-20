(ns heart-tracker.util
  (:require [cognitect.transit :as t]
            [om.next :as om]
            [goog.i18n.DateTimeFormat :as dtf]
            [goog.string :as gstring]
            [goog.string.format]
            [heart-tracker.state :refer [app-state]])
  (:import [goog.net XhrIo]
           [goog.i18n NumberFormat]))

(defn transit-post [url]
  (fn [{:keys [remote] :as ast} cb]
    (let [{:keys [query rewrite] :as ast} (om/process-roots remote)]
      (.send XhrIo url
             (fn [e]
               (this-as this
                 (cb (rewrite (t/read (om/reader) (.getResponseText this))))))
             "POST" (t/write (om/writer) query)
             (clj->js {"Content-Type" "application/transit+json"
                       "Authorize" (-> @app-state :current/user :user/emailAddress)})))))