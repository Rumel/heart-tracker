 (ns user
   (:require [com.stuartsierra.component :as component]
             [clojure.tools.namespace.repl :refer [refresh]]
             [heart-tracker.system :as app]))

 (defonce system nil)

(defn init []
  (alter-var-root #'system
                  (constantly
                    (app/new-dev-system {:port 8080
                                         :host "localhost"
                                         :uri "datomic:free://localhost:4334/heart-tracker"}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system (fn [s]
                             (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))