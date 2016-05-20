(ns heart-tracker.parser
  (:require [om.next.server :as om]
            [io.pedestal.log :refer [info]]
            [datomic.api :as d]
            [heart-tracker.model :refer [new-user-tx all-bp-results add-bp-result-tx]]))

(defmulti readf (fn [_ k _] k))
(defmulti mutate (fn [_ k _] k))

(defmethod readf :current/user
  [{:keys [db emailAddress]} k _]
  {:value {:user/emailAddress emailAddress
           :user/bpResults (all-bp-results db emailAddress)}})

(defmethod mutate 'add/bpResult
  [{:keys [emailAddress conn]} k {:keys [db/id
                                         systolic
                                         diastolic
                                         heartRate
                                         dateTaken :as params]}]
  (let [tempid (d/tempid :db.part/user)
        result @(d/transact conn
                            (add-bp-result-tx {:db/id tempid
                                               :user/emailAddress emailAddress
                                               :user/systolic     systolic
                                               :user/diastolic    diastolic
                                               :user/heartRate    heartRate
                                               :user/dateTaken    dateTaken}))
        db (:db-after result)
        realid (d/resolve-tempid db (:tempids result) tempid)]
    {:value  {:tempids {[:bpResults/by-id id] [:bpResults/by-id realid]}}
     :action (fn [])}))


(comment
  (def uri "datomic:free://localhost:4334/heart-tracker")
  (def conn (d/connect uri))

  ;; Query Examples
  (d/q '[:find ?user
         :in $database
         :where [$database ?user :user/emailAddress]
         [$database ?user :user/heartRate]] (d/db conn))

  (d/q '[:find ?user
         :where [?user :user/emailAddress ?email]] (d/db conn) "tyler@givestack.com")

  (d/q '[:find ?aname ?v
         :in $ ?user
         :where [?user ?a ?v]
         [?a :db/ident ?aname]] (d/db conn) [:user/emailAddress "tyler@givestack.com"])

  (d/transact conn [[:db.fn/retractEntity 17592186045449]])

  @(d/transact conn (new-user-tx "tyler@givestack.com"))

  @(d/transact conn (add-bp-result-tx {:user/emailAddress "tyler@givestack.com"
                                       :user/systolic     117
                                       :user/diastolic    58
                                       :user/heartRate    60
                                       :user/dateTaken    #inst"2016-05-16"}))
  (d/pull (d/db conn) '[:user/emailAddress :user/bpResults] [:user/emailAddress "tyler@givestack.com"])

  (def db-since (d/since (d/db conn) #inst"2016-05-21"))

  (all-bp-results (d/db conn) "tyler@givestack.com")

  ((om/parser {:read readf :mutate mutate}) {:emailAddress "tyler@givestack.com" :db (d/db conn) :conn conn}
    '[({:current/user
        [:user/emailAddress
         :user/bpResults]})])

  ((om/parser {:read readf :mutate mutate}) {:conn conn :emailAddress "tyler@givestack.com"} '[(add/bpResult {:user/systolic 112}
                                                                                                             :user/diastolic 80
                                                                                                             :user/heartRate 75
                                                                                                             :user/dateTaken #inst"2016-04-20")]))
