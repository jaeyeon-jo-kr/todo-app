(ns todo.sandbox
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as ring]
            [mount.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.coercion :as coercion]))

(def app
  (ring/ring-handler
   (ring/router
    ["/"
     #_["mirror" {:get (fn [req]
                         (resp/response (str req)))}]
     #_["mirror/:id" {:get (fn [req]
                             (resp/response (str req)))}]
     #_todo/routes]
    {:data  
     {:coercion   reitit.coercion.spec/coercion
      :muuntaja   m/instance
      :middleware []}}
    #_{:data {:middleware [wrap-reload
                         #_params/wrap-params
                         #_muuntaja/format-middleware
                         #_coercion/coerce-exceptions-middleware
                         #_coercion/coerce-request-middleware
                         #_coercion/coerce-response-middleware]}})
   #_(ring/create-default-handler)))

(comment
  (all-todos)
  (select-colors [1  3])
  (set [1 2 3])

  (d/q '[:find ?e ?t ?c
         :where
         [?e _ _]
         [(get-else $ ?e :todo/title-color-id "N/A") ?t]
         [(get-else $ ?e :color/id "N/A") ?c]] @db)

  (d/q '[:find ?e
         :where [?e :todo/id]] @db)

  (d/q '[:find ?e
         :in [?e ...]] @db)

  (def todos)

  (clojure.set/union)

  (def color (d/q '[:find ?id ?name ?value
                    :where
                    [?todo :color/id ?id]
                    [?todo :color/name ?name]
                    [?todo :color/value ?value]]
                  @db))

  (clojure.set/union)



  (d/q '[:find ?id ?title ?completed
         :where
         [?todo :todo/title ?title]
         [?todo :todo/id ?id]
         [?todo :todo/completed ?completed]
         [(get-else $ ?todo :todo/title-color-id "N/A") ?title-color-id]
         [(get-else $ ?todo :color/id "N/A") ?title-color-id]
         [(get-else $ ?color :color/name "N/A") ?color-name]] @db)


  (def find-expr
    '[:find (pull ?e details-expr)
      :in $ details-expr
      :where [?e :todo/title]])
  (def details-expr [:todo/title :todo/completed])
  (d/q find-expr @db details-expr)

  (def find-expr-2
    '[:find (pull ?e details-expr)
      :in $ details-expr
      :where
      [?e :todo/title]
      [?todo :todo/id ?todo-id]
      [?todo-color :todo-color/todo-id ?todo-id]
      [?todo-color :todo-color/color-id ?color-id]])
  (def details-expr-2 [:todo/title :todo/completed :todo-color/color-id])
  (d/q find-expr-2 @db details-expr-2)

  (d/q '[:find ?artist-name ?year
         :in $ [?artist-name ...]
         :where [?artist :artist/name ?artist-name]
         [(get-else $ ?artist :artist/startYear "N/A") ?year]]
       @db, ["Crosby, Stills & Nash" "Crosby & Nash"])

  (d/q '[:find ?todo-id
         :where
         [?todo :todo/id ?todo-id]]
       @db)

  (d/q '[:find ?todo-id
         :where
         [?todo :color/id ?todo-id]]
       @db)

  (d/q '[:find ?todo-id ?todo-color
         :where
         [?todo :todo/id]
         [?todo :todo/id ?todo-id]
         [?todo-color :todo-color/todo-id ?todo-id]]
       @db)

  (d/q '[:find ?todo-id ?title-color-id
         :where
         [?todo :todo/id]
         [?todo :todo/id ?todo-id]
         [(get-else $ ?todo :todo/title-color-id ::none) ?title-color-id]]
       @db)

  (d/q '[:find ?todo-id ?title-color-id
         :where
         [?todo :todo/id]
         [?todo :todo/id ?todo-id]
         [(get-else $ ?todo :todo/title-color-id ::none) ?title-color-id]
         [?color-id :color/id ?title-color-id]]
       @db)

  (def details-expr [:todo/id :todo/title])
  (def find-expr `[:find (pull ?e details-expr)
                   :in $ details-expr
                   :where [?e :todo/id]])
  (d/q find-expr db details-expr)

  (d/q '[:find ?todo-id ?title-color-id
         :where
         [?color :color/id]
         [?todo :todo/id ?todo-id]
         [(get-else $ ?todo :todo/title-color-id ::none) ?title-color-id]]
       @db)

  (d/q '[:find ?todo-id ?color-id
         :where
         [?todo :todo/id]
         [?todo :todo/id ?todo-id]
         [(get-else $ ?todo-color :todo-color/-id ::nil) ?color-id]]
       @db)
  (d/q '[:find (pull ?t [:todo/title-color-id])
         :in $ ?todo-id
         :where
         [?t :todo/id ?todo-id]]
       @db "None")

  (d/q '[:find ?todo-id ?color-id
         :where
         [?t :todo/title-color-id ?color-id]
         [?t :todo/id ?todo-id]]
       @db))

