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

(defn get-proper-id
  [id-coll]
  (reduce (fn [cand id]
            (if (= cand id)
              (inc cand)
              (reduced cand))) 1 id-coll))

(defn id-list
  []
  (map first
       (d/q `[:find ?id
              :where
              [?e :todo/id ?id]] (db/get-db))))

(defn handle-post
  [{{title :title} :body-params}] 
  (try
    (r/response
     (do
       (d/transact db/conn
                   [{:todo/id (bigint (get-proper-id (id-list)))
                     :todo/title title
                     :todo/completed false}])
       (json/write-str "success!")))
    (catch Exception e
      (response (str (.getMessage e)
                     {:title title})))))

(comment 
  (d/transact db/conn
              [{:todo/id (bigint (get-proper-id (id-list)))
                :todo/title "dasdf"
                :todo/completed false}]))

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

(comment 
  
  
  
  )


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
   ["/new" {:post 
            {:coercion reitit.coercion.malli/coercion
             :parameters {:body-params
                          [:map
                           [:id int?]
                           [:title string?]
                           [:completed boolean?]]}
             :handler handle-post}}]
   ["/update" {:post
               {:coercion reitit.coercion.malli/coercion
                :parameters {:body-params
                             [:map
                              [:id int?]
                              [:title string?]
                              [:completed boolean?]]}
                :handler handle-update}}]])