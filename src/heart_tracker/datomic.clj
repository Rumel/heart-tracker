(ns heart-tracker.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [clojure.java.io :as io])
  (:import datomic.Util))

(defrecord Datomic [uri initial-schema conn]
  component/Lifecycle
  (start [component]
    (d/create-database uri)
    (let [c (d/connect uri)]
      @(d/transact c initial-schema)
      (assoc component :conn c)))
  (stop [component]))

(defn new-datomic-component
  [uri]
  (Datomic.
    uri
    (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
    nil))

