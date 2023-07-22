(ns todo.core
  (:require
   [todo.db]
   [clojure.tools.logging :as log]
   [ring.middleware.reload :refer [wrap-reload]]
   [mount.core :as m]
   [ring.adapter.jetty :as jetty]
   [todo.app :refer [app]]))

(defn reloaded-app
  [app]
  (wrap-reload #'app))

(m/defstate server 
  "Start server"
  :start (jetty/run-jetty
          #'app
          {:port 3022 :join? false})
  :stop #(.stop %))


(defn -main
  [& params]
  (log/info "start server")
  (m/start))

(comment
  (m/stop)

  )

