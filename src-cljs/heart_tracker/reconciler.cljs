(ns heart-tracker.reconciler
  (:require [om.next :as om]
            [heart-tracker.parser :as p]
            [heart-tracker.state :as s]))

(def reconciler
  (om/reconciler
    {:parser p/parser
     :state s/app-state
     :normalize true}))


