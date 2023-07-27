(ns todo.todo
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [reitit.coercion.malli]
            [ring.util.response :as r :refer [response]]
            [todo.db :refer [conn db]]
            [clojure.core :as c]))

(defn get-proper-id
  [id-coll]
  (reduce (fn [cand id]
            (if (= cand id)
              (inc cand)
              (reduced cand))) 1 id-coll))

(defn format-item
  [[id title completed]]
  {:id id
   :title title
   :completed completed})

(defn id-list
  []
  (map first
       (d/q `[:find ?id
              :where
              [?e :todo/id ?id]] @db)))

(defn handle-post
  [{{title :title} :body-params}] 
  (try
    (r/response
     (do
       (d/transact @conn
                   [{:todo/id (get-proper-id (id-list))
                     :todo/title title
                     :todo/completed false}])
       (json/write-str "success!")))
    (catch Exception e
      (response (str (.getMessage e)
                     {:title title})))))

(comment 
  (d/transact @conn
              [{:todo/id (get-proper-id (id-list))
                :todo/title "test"
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
               @db))
      {:escape-unicode true}))))


(defn handle-get-all
  [request]
  (r/response
   (json/write-str
    (map format-item
         (d/q `[:find ?id ?title ?completed ?name ?value
                :where
                [?todo :todo/title ?title]
                [?todo :todo/id ?id]
                [?todo :todo/completed ?completed]
                [(get-else $ ?todo-color :todo-color/todo-id "Unknown") ?id]
                [?todo-color :todo-color/color-id ?color-id]
                [?color :color/id ?color-id]
                [?color :color/name ?name]
                [?color :color/value ?value]
                ] @db))
    {:escape-unicode true})))


(defn query-all-todos
  []
  (set
   (map #(assoc {}
            :todo/id (first %)
            :todo/title (second %)
            :todo/completed (nth % 2)
            :todo/title-color-id (nth % 3))
    (d/q '[:find ?id ?title ?completed ?title-color-id
           :where
           [?todo :todo/title ?title]
           [?todo :todo/id ?id]
           [?todo :todo/completed ?completed]
           [(get-else $ ?todo :todo/title-color-id :none) ?title-color-id]]
         @db))))

(defn query-all-colors
  []
  (set
   (map #(assoc {}
                :color/id (first %)
                :color/name (second %)
                :color/value (nth % 2))
        (d/q '[:find ?id ?name ?value
               :where
               [?color :color/name ?name]
               [?color :color/id ?id]
               [?color :color/value ?value]]
             @db)))
  )

(defn all-todos
  []
  (let [todos (query-all-todos)
        colors (query-all-colors)]
    (clojure.set/union
     (clojure.set/select (comp #{:none}
                               :todo/title-color-id) todos)
     (clojure.set/join todos colors {:todo/title-color-id :color/id}))))


(comment 
  (all-todos)
  (select-colors [1  3])
  (set [1 2 3])
  
  (d/q '[:find ?e ?t ?c 
         :where 
         [?e _ _]
         [(get-else $ ?e :todo/title-color-id "N/A") ?t]
         [(get-else $ ?e :color/id "N/A") ?c]
         ]@db)
  
  (d/q '[:find ?e 
         :where [?e :todo/id]] @db) 
  
  (d/q '[:find ?e
         :in [?e ...]] @db) 
  
  (def todos 
    )
  
  (clojure.set/union )
  
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
  (d/q find-expr @ db details-expr)

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
       @db)
  )

(defn handle-update
  [{{id :id
     title :title
     completed :completed} :body-params :as req}] 
  (if id
    (r/response
     (do
       (d/transact @conn [{:todo/id id
                           :todo/title title
                           :todo/completed completed}])
       (json/write-str {:result "success"})))
     
    (r/not-found (r/response (str req))))
  )

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

