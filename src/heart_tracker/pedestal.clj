(ns heart-tracker.pedestal
  (:require [com.stuartsierra.component :as component]
            [heart-tracker.service :as service]
            [io.pedestal.http :as server]
            [heart-tracker.util :as util]))

(defrecord Pedestal [host port service-map datomic]
  component/Lifecycle
  (start [component]
    (assoc component :pedestal
                     (-> service-map
                         (update-in [::server/interceptors]
                                    conj
                                    (util/wrap-datomic datomic))
                         server/create-server
                         server/start)))
  (stop [component]
    (when-let [pedestal (:pedestal component)]
      (server/stop pedestal)
      (dissoc component :pedestal))))

(defn new-pedestal-dev-server
  [host port]
  (let [service-map (-> service/service
                        (merge {:env                     :dev
                                ::server/join?           false
                                ::server/routes #(deref #'service/routes)
                                ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
                        server/default-interceptors
                        server/dev-interceptors)]
    (Pedestal.
      host
      port
      service-map
      nil)))
