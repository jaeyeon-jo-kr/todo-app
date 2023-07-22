(ns todo.color
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [reitit.coercion.malli]
            [ring.util.response :as r :refer [response]]
            [todo.db :as db]))

(defn handle-new
  [{{description :description
     value :value} :body-params}]
  (try
    (r/response
     (do
       (d/transact db/conn
                   [{:color/id (random-uuid)
                     :color/description description
                     :color/value value}])
       (json/write-str "success!")))
    (catch Exception e
      (response (str (.getMessage e))))))

(defn format-item
  [[id description value]]
  {:id id
   :description description
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
                db/db))
      {:escape-unicode true}))))


(defn handle-get-all
  [request]
  (r/response
   (json/write-str
    (map format-item
         (d/q `[:find ?id ?description ?value
                :where
                [?e :color/id ?id]
                [?e :color/description ?description]
                [?e :color/value ?value]] db/db))
    {:escape-unicode true})))


(defn handle-update
  [{{id :id 
     description :description
     value :value} :body-params :as req}]
  (if id
    (r/response
     (do
       (d/transact db/conn
                   [{:color/id (parse-uuid id)
                     :color/description description
                     :color/value value}])
       (json/write-str {:result "success"})))

    (r/not-found (r/response (str req)))))


(def routes
  ["/color"
   ["/all" {:get {:handler handle-get-all}}]
   #_["/get/:id" {:get {:handler handle-get}}]
   ["/new" {:post
            {:coercion reitit.coercion.malli/coercion
             :parameters {:body-params
                          [:map
                           [:description string?]
                           [:value string?]]}
             :handler handle-new}}]
   ["/update" {:post
               {:coercion reitit.coercion.malli/coercion
                :parameters {:body-params
                             [:map
                              [:id uuid?]
                              [:description string?]
                              [:value string?]]}
                :handler handle-update}}]])