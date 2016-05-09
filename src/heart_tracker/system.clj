(ns heart-tracker.system
  (:require [com.stuartsierra.component :as component]
            [heart-tracker.datomic :as datomic]
            [heart-tracker.pedestal :as pedestal]))

(defn new-dev-system
  [{:keys [host port uri]}]
  (component/system-map
    :datomic (datomic/new-datomic-component uri)
    :pedestal (component/using
                (pedestal/new-pedestal-dev-server host port)
                [:datomic])))

