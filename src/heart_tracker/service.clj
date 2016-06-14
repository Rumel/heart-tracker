(ns heart-tracker.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :refer [info]]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [clojure.java.io :as io]
            [om.next.server :as om]
            [heart-tracker.util :refer [wrap-authorize]]
            [heart-tracker.parser :refer [readf mutate]]
            [io.pedestal.interceptor.helpers :as interceptor]
            [cognitect.transit :as transit]
            [heart-tracker.model :as model])
  (:import (java.io OutputStream)
           (heart_tracker.model BpResult)))

(defn api
  [request]
  (info :email (-> request :emailAddress))
  (let [payload (:transit-params request)
        resp ((om/parser {:read   readf
                          :mutate mutate}) {:db (:db request)
                                                :emailAddress (:emailAddress request)
                                                       :conn (:connection request)} payload)]
    (info :payload payload)
    {:status 200
     :body resp}))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(def transit-json-body
  (interceptor/on-response
    ::transit-json-body
    (fn [res]
      (let [body (:body res)
            content-type (get-in res [:headers "Content-Type"])]
        (if (and  (coll? body) (not content-type))
          (-> res
              (ring-resp/content-type "application/transit+json;charset=UTF-8")
              (assoc :body (fn [^OutputStream output-stream]
                             (transit/write (transit/writer
                                              output-stream
                                              :json
                                              {:handlers {BpResult model/BpResultWriteHandler}}) body)
                             (.flush output-stream))))
          res)))))

(defn home-page
  [request]
  (ring-resp/response
    (slurp (io/file
             (io/resource "public/index.html")))))

(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/api" ^:interceptors [transit-json-body wrap-authorize] {:any api}]
     ["/about" {:get about-page}]]]])

;; Consumed by heart-tracker.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})

