(ns heart-tracker.parser
  (:require [om.next :as om]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :app/page
  [{:keys [state]} _ _]
  (let [st @state]
    {:value (get st :app/page)}))

(defmethod read :current/user
  [{:keys [state query]} k _]
  (println "reading")
  (let [st @state]
    {:value (om/db->tree query (get st k) st)}))

(defmethod mutate :default
  [_ k _]
  (println "No mutation method for key: " k))

(defmethod mutate 'change/page
  [{:keys [state]} _ {:keys [page]}]
  {:value {:keys [:app/page]}
   :action (fn []
             (swap! state assoc :app/page page))})

(defmethod mutate 'add/bpResult
  [{:keys [state]} _ {:keys [id systolic diastolic heartRate]}]
  {:value  {:keys [:user/bpResults]}
   :action (fn []
             (swap! state
                    (fn [st]
                      (-> st
                          (update :bpResults/by-id assoc id
                                  {:user/systolic systolic
                                   :user/diastolic diastolic
                                   :user/heartRate heartRate
                                   :user/dateTaken (js/Date.)
                                   :db/id id})
                          (update-in [:current/user :user/bpResults]
                                     conj [:bpResults/by-id id])))))})
(def parser
  (om/parser {:read read
              :mutate mutate}))