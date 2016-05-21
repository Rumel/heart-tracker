(ns heart-tracker.state)

(def app-state
  (atom
    {:app/page :dashboard
     :current/user
                      {:user/emailAddress "test@test.com"
                       :user/apiKey       "abc1234"
                       :user/bpResults    [[:bpResults/by-id 1]
                                           [:bpResults/by-id 2]]}
     :bpResults/by-id {1 {:db/id          1
                          :user/systolic  117
                          :user/diastolic 80
                          :user/heartRate 60
                          :user/dateTaken (js/Date.)}
                       2 {:db/id          2
                          :user/systolic  112
                          :user/diastolic 75
                          :user/heartRate 65
                          :user/dateTaken (js/Date.)}}}))