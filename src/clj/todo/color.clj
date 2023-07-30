(ns todo.color
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [reitit.coercion.malli]
            [ring.util.response :as r :refer [response]]
            [todo.db :refer [conn db]]))

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
              [?e :color/id ?id]] @db)))


(defn handle-new
  [{{name :name
     value :value} :body-params}]
  (try
    (r/response
     (do
       (d/transact @conn
                   [{:color/id (get-proper-id (id-list))
                     :color/name name
                     :color/value value}])
       (json/write-str "success!")))
    (catch Exception e
      (response (str (.getMessage e))))))

(defn format-item
  [[id name value]]
  {:id id
   :name name
   :value value})

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
         (d/q `[:find ?id ?name ?value
                :where
                [?e :color/id ?id]
                [?e :color/name ?name]
                [?e :color/value ?value]] @db))
    {:escape-unicode true})))


(defn handle-update
  [{{id :id 
     name :name
     value :value} :body-params :as req}]
  (if id
    (r/response
     (do
       (d/transact @conn
                   [{:color/id id
                     :color/name name
                     :color/value value}])
       (json/write-str {:result "success"})))

    (r/not-found (r/response (str req)))))


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
             @db))))



(def routes
  ["/color"
   ["/all" {:get {:handler handle-get-all}}]
   #_["/get/:id" {:get {:handler handle-get}}]
   ["/new" {:post
            {:coercion reitit.coercion.malli/coercion
             :parameters {:body-params
                          [:map
                           [:name string?]
                           [:value string?]]}
             :handler handle-new}}]
   ["/update" {:post
               {:coercion reitit.coercion.malli/coercion
                :parameters {:body-params
                             [:map
                              [:id number?]
                              [:name string?]
                              [:value string?]]}
                :handler handle-update}}]])

