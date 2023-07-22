(ns todo.init-db
  (:require [datomic.api :as d]
            [todo.db :as db]
            [mount.core :as m]))

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

   {:db/ident :color/description
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
    :color/description "color-1"
    :color/value "#FF1122"}
   {:color/id 2
    :color/description "color-2"
    :color/value "#311122"}
   {:todo-color/todo-id 1
    :todo-color/color-id 1}
   {:todo-color/todo-id 2
    :todo-color/color-id 2}
   #_[:db/add (bigint 1) :todo-color/color-id color-id1]
   #_[:db/add (bigint 2) :todo-color/color-id color-id2]])

(def add-data
  [])

(comment
  (m/stop #'db/db #'db/conn)
  (d/delete-database db/uri)
  (d/create-database db/uri)
  (m/start #'db/db #'db/conn)
  (d/transact db/conn todo-schema)
  (d/transact db/conn sample-data)
  (d/q `[:find ?id
         :where [?e :todo/id ?id]]
       db/db)
  

  (d/q `[:find ?id ?title ?completed ?description ?value
         :where
         [?todo :todo/title ?title]
         [?todo :todo/id ?id]
         [?todo :todo/completed ?completed]
         [?todo-color :todo-color/color-id ?color-id]
         [?todo-color :todo-color/todo-id ?id]
         [?color :color/id ?color-id]
         [?color :color/description ?description]
         [?color :color/value ?value]] db/db)
  
  (d/q `[:find ?id
         :where
         [?todo :todo/id ?id]
         ] db/db)
  
  (d/q `[:find ?id
         :where
         [?todo :color/id ?id]] db/db)
  
  (d/q `[:find ?color-id ?id
     :where
     [?todo-color :todo-color/color-id ?color-id]
     [?todo-color :todo-color/todo-id ?id]]
   db/db
   )
  )