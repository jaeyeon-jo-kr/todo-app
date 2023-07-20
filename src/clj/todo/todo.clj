(ns todo.todo
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [reitit.coercion.malli]
            [ring.util.response :as r :refer [response]]
            [todo.db :as db]))

(defn format-item
  [[id title completed]]
  {:id id
   :title title
   :completed completed})

(comment 
 (json/write-str {:a.b "abc"}))


(defn handle-post
  [{{id :id
     title :title
     completed :completed} :body-params}]
  (log/debug id title completed)
  (try
    (r/response
     (do
       (d/transact db/conn
                   [{:todo/id (bigint id)
                     :todo/title title
                     :todo/completed completed}])
       "success!"))
    (catch Exception e
      (response (str (.getMessage e)
                     {:id id :title title :completed completed})))))

(comment 
  
  (r/response (d/transact db/conn
                          [{:todo/id (bigint 3)
                            :todo/title "logging"
                            :todo/completed false}]))
  )

(defn handle-get
  [{{id :id} :path-params}]
  (let [id (bigint id)]
    (r/response
     (json/write-str
      (map format-item
           (d/q `[:find ?id ?title
                  :where
                  [?e :todo/title ?title]
                  [?e :todo/id ?id]
                  [?e :todo/id ~id]
                  [?e :todo/completed ?completed]]
                (db/get-db)))
      {:escape-unicode true}))))


(defn handle-get-all
  [request]
  (r/response
   (json/write-str
    (map format-item
         (d/q `[:find ?id ?title ?completed
                :where
                [?e :todo/title ?title]
                [?e :todo/id ?id]
                [?e :todo/completed ?completed]] (db/get-db)))
    {:escape-unicode true})))


(defn handle-update
  [{{id :id
     title :title
     completed :completed} :body-params :as req}] 
  (if id
    (r/response
     (do
       (d/transact db/conn [{:todo/id (bigint id)
                             :todo/title title
                             :todo/completed completed}])

       (json/write-str {:result "success"})))
     
    (r/not-found (r/response (str req))))
  )

(comment 
  (r/not-found (r/response "")))

(def routes
  ["/todo"
   ["/all" {:get {:handler handle-get-all}}]
   ["/get/:id" {:get {:handler handle-get}}]
   ["/new" {:post {:handler handle-post}}]
   ["/update" {:post
               {:coercion reitit.coercion.malli/coercion
                :parameters {:body-params
                             [:map
                              [:id int?]
                              [:title string?]
                              [:completed boolean?]]}
                :handler handle-update}}]])