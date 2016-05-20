(ns heart-tracker.util
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [datomic.api :as d]))

(defn wrap-datomic
  [datomic]
  (interceptor
    {:enter (fn [ctx]
              (let [conn (:conn datomic)
                    db (d/db conn)]
                (-> ctx
                    (assoc-in [:request :connection] conn)
                    (assoc-in [:request :db] db))))}))

(def wrap-authorize
  (interceptor
    {:enter (fn [ctx]
              (if-let
                [email (get-in ctx [:request :headers "authorize"])]
                (assoc-in ctx [:request :emailAddress] email)
                ctx))}))
