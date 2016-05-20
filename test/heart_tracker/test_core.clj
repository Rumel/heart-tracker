(ns heart-tracker.test-core
  (:require [datomic.api :as d]
            [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import datomic.Util))

(def ^:dynamic *conn* nil)
(def ^:dynamic *db* nil)

(defn fresh-database []
  (let [db-name (gensym)
        db-uri (str "datomic:mem://" db-name)]
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      @(d/transact conn
                   (first (Util/readAll (io/reader (io/resource "data/schema.edn")))))
      conn)))

(defn with-database [f]
  (let [conn (fresh-database)]
    (binding [*conn* conn
              *db* (d/db conn)]
      (f))))

(use-fixtures :each with-database)

(deftest this-test-gets-a-database
  (is (not (nil? *conn*)))
  (is (not (nil? *db*)))
  (is (= 0 (count (d/q '[:find ?u
                         :where [_ :user/emailAddress ?u]] *db*)))))
