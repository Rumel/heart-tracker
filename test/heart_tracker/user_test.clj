(ns heart-tracker.user-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [heart-tracker.model :as model]
            [heart-tracker.test-core :refer :all]))

(use-fixtures :each with-database)

(deftest test-adding-new-bp-results
  (let [db (->> (model/add-bp-result-tx {:user/systolic  110
                                         :user/diastolic 85
                                         :user/heartRate 55
                                         :user/dateTaken (java.util.Date.)
                                         :user/emailAddress "test@test.net"})
                (d/with (d/db *conn*))
                :db-after)
        user (model/user db "test@test.net")]
    (is (= 110
           (:user/systolic user)))
    (is (= 85
           (:user/diastolic user)))))