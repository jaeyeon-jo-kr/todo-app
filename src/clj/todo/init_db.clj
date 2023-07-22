(ns todo.init-db
  (:require [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/todo")

(def conn (d/connect uri))

(def todo-schema
  [{:db/ident :todo/id
    :db/valueType :db.type/bigint
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "Todo item id"}

   {:db/ident :todo/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Todo title"}

   {:db/ident :todo/completed
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Todo completed."}])

(comment 
  (d/delete-database uri)
  (d/create-database uri)
  (d/transact conn todo-schema)
  (d/transact conn [{:db/id :todo/id
                     :db/unique :db.unique/identity}])
  (d/transact conn [{:db/id :todo/id
                     :db/index true}]) 
  (d/transact conn [{:db/id :todo/id
                     :db/cardinality :db.cardinality/one}])
  (d/transact conn [{:db/id :todo/id
                     :db/cardinality :db.cardinality/one}])
  (d/transact conn 
              [[:db/retract :todo/id 
                :db/unique :db.unique/identity]])
  (d/transact
   conn
   {:tx-data
    [[:db/retract [:todo/id 1]]
     [:db/add "datomic.tx"
      :db/doc "remove incorrect assertion"]]})
  
  )

(defn create-db! 
  [] 
  (d/create-database uri))

(defn create-todo-schema!
  []
  (d/transact conn todo-schema))
