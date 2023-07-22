(ns todo.db
  (:require [mount.core :as m]
            [datomic.api :as d]))

(m/defstate conn
  :start (d/connect "datomic:dev://localhost:4334/todo"))

(defn get-db
  []
  (d/db conn))

(comment
  (get-db)
  (m/stop)
  (m/start)
  )


