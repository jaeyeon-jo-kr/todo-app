(ns todo.db
  (:require [datomic.api :as d]
            [todo.states :refer [db conn]]))



(def uri "datomic:dev://localhost:4334/todo")

(def todo-schema
  [{:db/ident :todo/id
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db/doc "Todo item id"}

   {:db/ident :todo/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Todo title"}
   
   {:db/ident :todo/title-color-id
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Todo title color id."}

   {:db/ident :todo/completed
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Todo completed."}
   
   {:db/ident :color/id
    :db/valueType :db.type/long
    :db/unique :db.unique/identity
    :db/index true
    :db/cardinality :db.cardinality/one
    :db/doc "Custom Color uuid"}

   {:db/ident :color/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :color/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(def sample-data
  [{:todo/id 1
    :todo/title "title 1"
    :todo/completed false}
   {:todo/id 2
    :todo/title "title 2"
    :todo/completed true}
   {:todo/id 3
    :todo/title "title 3"
    :todo/completed true
    :todo/title-color-id 1}
   {:color/id 1
    :color/name "color-1"
    :color/value "#FF1122"}
   {:color/id 2
    :color/name "color-2"
    :color/value "#311122"}
   {:color/id 3
    :color/name "color-3"
    :color/value "#311162"}])

(defn refresh
  []
  (reset! conn (d/connect uri))
  (reset! db (d/db @conn)))


(defn start!
  []
  (d/create-database uri)
  (reset! conn (d/connect uri))
  (reset! db (d/db @conn))
  (d/transact @conn todo-schema)
  (d/transact @conn sample-data)
  (reset! conn (d/connect uri))
  (reset! db (d/db @conn)))

(defn stop!
  []
  (d/delete-database uri)
  (d/release @conn))

(comment 
  (start!)
  (stop!))

