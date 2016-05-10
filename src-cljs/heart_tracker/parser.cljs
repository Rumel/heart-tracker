(ns heart-tracker.parser
  (:require [om.next :as om]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :current/user
  [{:keys [state query]} k _]
  (println "reading")
  (let [st @state]
    {:value (get st k)}))

(defmethod mutate :default
  [_ k _]
  (println "No mutation method for key: " k))

(def parser
  (om/parser {:read read
              :mutate mutate}))