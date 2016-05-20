(ns heart-tracker.reconciler
  (:require [om.next :as om]
            [heart-tracker.parser :as p]
            [heart-tracker.state :as s]
            [heart-tracker.util :as u]))

(def reconciler
  (om/reconciler
    {:parser p/parser
     :state s/app-state
     :send (u/transit-post "/api")
     :id-key :db/id
     :normalize true}))


