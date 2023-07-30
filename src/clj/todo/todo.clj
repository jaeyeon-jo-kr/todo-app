(ns todo.todo
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [reitit.coercion.malli]
            [ring.util.response :as r :refer [response]]
            [todo.db :refer [conn db]]
            [clojure.core :as c]
            [todo.color :as color]
            [clojure.set :as set]))

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


(defn all-todos
  []
  (let [todos (query-all-todos)
        colors (color/query-all-colors)]
    (set/union
     (set/select
      (comp #{:none} :todo/title-color-id) todos)
     (set/join todos colors {:todo/title-color-id :color/id}))))

(defn handle-get-all
  [request]
  (r/response
   (json/write-str
    (all-todos)
    {:escape-unicode true})))

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

    (r/not-found (r/response (str req)))))

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

