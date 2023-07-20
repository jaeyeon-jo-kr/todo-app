(ns todo.db
  (:require [mount.core :as m]
            [datomic.api :as d]
            [todo.db :as db]))

(m/defstate conn
  :start (d/connect "datomic:dev://localhost:4334/todo"))

(defn get-db
  []
  (d/db conn))
