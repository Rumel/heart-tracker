(ns heart-tracker.model
  (:require [datomic.api :as d]))

(defn user-id
  [db email]
  (some->
    (d/q '[:find ?u
           :in $ ?email
           :where [?u :user/emailAddress ?email]] db email)
    ffirst))

(defn new-user-tx
  [email]
  [{:db/id             (d/tempid :db.part/user)
    :user/emailAddress email}])

(defn user
  [db email]
  (d/entity db [:user/emailAddress email]))

(defn user-bp-results
  [db email]
  (-> (user db email)
      (select-keys [:user/dateTaken :user/systolic :user/diastolic :user/heartRate])))

(defn add-bp-result-tx
  [bp-result]
  (let [tx [(merge {:db/id (d/tempid :db.part/user)} bp-result)]]
    (println "BP -> " tx)
    tx))


(defn- tx-log->map
  "Takes a transaction log and converts it to a map. Key is tx-id"
  [tx-log]
  (apply merge-with
         merge
         (map #(assoc {} (nth % 0) {(nth % 1) (nth % 2)}) tx-log)))


(defn- log-map->vector
  "Takes a mapped transaction log and converts it to a vector of maps."
  [log-map]
  (mapv (fn [[k v]] (merge {:db/id k} v)) log-map))

(defn bp-tx-log-history
  [db email]
  (let [history (d/history db)]
     (d/q '[:find ?tx ?aname ?v ?inst
            :in $ ?e
            :where [?e ?a ?v ?tx true]
                   [?tx :db/txInstant ?inst]
                   [?a :db/ident ?aname]
                   [(not= ?aname :user/emailAddress)]]
          history [:user/emailAddress email])))

(defn all-bp-results
  "Returns the transaction history of all the BP results."
  [db email]
  (-> (bp-tx-log-history db email)
      tx-log->map
      log-map->vector))
