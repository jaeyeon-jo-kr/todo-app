(ns todo.init-db
  (:require [datomic.api :as d]))

(def url (d/connect "datomic:dev://localhost:4334/todo"))
(def conn (d/connect url))

(def todo-schema
  [{:db/ident :todo/id
    :db/valueType :db.type/bigint
    :db/cardinality :db.cardinality/one
    :db/doc "Todo item id"}

   {:db/ident :todo/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Todo title"}

   {:db/ident :todo/completed
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Todo completed."}])

(defn create-db! 
  [] 
  (d/create-database url))

(defn create-todo-schema!
  []
  (d/transact conn todo-schema))
