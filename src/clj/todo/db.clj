(ns todo.db
  (:require [mount.core :as m]
            [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/todo")

(m/defstate conn
  :start (d/connect uri))

(m/defstate db
  :start (d/db conn))