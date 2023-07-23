(ns todo.db
  (:require [datomic.api :as d]))

(defonce db (atom nil))
(defonce conn (atom nil))

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
    :db/cardinality :db.cardinality/one}

   {:db/ident :todo-color/todo-id
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/ref
    :db/doc "Color-todo todo-id"}

   {:db/ident :todo-color/color-id
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/ref
    :db/doc "Color-todo todo-id"}])

(def sample-data
  [{:todo/id 1
    :todo/title "title 1"
    :todo/completed false}
   {:todo/id 2
    :todo/title "title 2"
    :todo/completed true}
   {:color/id 1
    :color/name "color-1"
    :color/value "#FF1122"}
   {:color/id 2
    :color/name "color-2"
    :color/value "#311122"}
   {:todo-color/todo-id 1
    :todo-color/color-id 1}
   {:todo-color/todo-id 2
    :todo-color/color-id 2}
   #_[:db/add (bigint 1) :todo-color/color-id color-id1]
   #_[:db/add (bigint 2) :todo-color/color-id color-id2]])


(defn start!
  []
  (reset! conn (d/connect uri))
  (reset! db (d/db @conn))
  (d/create-database uri)
  (d/transact @conn todo-schema)
  (d/transact @conn sample-data))

(defn stop!
  []
  (d/delete-database uri)
  (d/release @conn))

