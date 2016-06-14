(ns heart-tracker.parser
  (:require [om.next.server :as om]
            [io.pedestal.log :refer [info]]
            [datomic.api :as d :refer [q transact]]
            [heart-tracker.model :refer [new-user-tx all-bp-results add-bp-result-tx bp-tx-log-history]]))

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

  ;; Create a new user
  @(d/transact conn (new-user-tx "test@test.com"))


  ;; Get the database value
  (def db (d/db conn))

  ;; Query Examples

  ;; Query API uses the "q" function

  ;; Include the $database input source
  (q '[:find ?user
       :in $database
       :where [$database ?user :user/emailAddress]
              [$database ?user :user/heartRate]]
     db)

  ;; Lookup user by email address
  (q '[:find ?user
       :in $ ?email
       :where [?user :user/emailAddress ?email]]
     db
     "test@test.com")

  ;; Get attributes of a user using the "identity lookup"
  (q '[:find ?aname ?v
       :in $ ?user
       :where [?user ?a ?v]
              [?a :db/ident ?aname]]
     db
     [:user/emailAddress "test@test.com"])

  ;; Example Transactions
  ;;
  @(transact conn [[:db.fn/retractEntity 17592186045449]])

  @(transact conn (new-user-tx "test@test.com"))

  @(transact conn (add-bp-result-tx {:user/emailAddress "test@test.com"
                                     :user/systolic     117
                                     :user/diastolic    58
                                     :user/heartRate    60
                                     :user/dateTaken    #inst"2016-05-16"}))

  ;; Example pull query
  (d/pull (d/db conn) '[:user/emailAddress :user/bpResults] [:user/emailAddress "test@test.com"])

  ;; Get the database as a value "since"...
  (def db-since (d/since (d/db conn) #inst"2016-05-19"))


  (bp-tx-log-history db "test@test.com")

  (all-bp-results db "test@test.com")

  ((om/parser {:read readf :mutate mutate}) {:emailAddress "test@test.com" :db (d/db conn) :conn conn}
    '[({:current/user
        [:user/emailAddress
         :user/bpResults]})])

  ((om/parser {:read readf :mutate mutate}) {:conn conn :emailAddress "test@test.com"} '[(add/bpResult {:user/systolic 112
                                                                                                             :user/diastolic 80
                                                                                                             :user/heartRate 75
                                                                                                             :user/dateTaken #inst"2016-04-20"})]))