(ns todo.init-db
  (:require [datomic.api :as d]
            [todo.db :as db]
            [mount.core :as m]))





(comment
  (m/stop #'db/db #'db/conn)
  (d/delete-database db/uri)
  (d/create-database db/uri)
  (m/start #'db/db #'db/conn)
  
  (d/transact db/conn sample-data)
  (d/q `[:find ?id
         :where [?e :todo/id ?id]]
       db/db)
  

  (d/q `[:find ?id ?title ?completed ?name ?value
         :where
         [?todo :todo/title ?title]
         [?todo :todo/id ?id]
         [?todo :todo/completed ?completed]
         [?todo-color :todo-color/color-id ?color-id]
         [?todo-color :todo-color/todo-id ?id]
         [?color :color/id ?color-id]
         [?color :color/name ?name]
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