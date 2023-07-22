(ns todo.todo
  (:require [re-frame.core :as r]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(r/reg-event-db
 ::todo-load-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db :todo-list [])))

(r/reg-event-db
 ::todo-load-result
 (fn [db [_ result]]
   (js/console.debug "current db : " db)
   (assoc db :todo-list result)))

(r/reg-event-fx
 ::load
 (fn [{:keys [db]} _] 
   {:db db
    :http-xhrio
    {:method :get
     :uri "http://localhost:3022/todo/all"
     :timeout 8000 
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::todo-load-result]
     :on-failure [::todo-load-error]}}))

(r/reg-sub
 :todo 
 (fn [db]
   (js/console.debug "subscribe db : "
                     (:todo-list db))
   (:todo-list db)))

(r/reg-event-fx
 ::update
 (fn [{:keys [db]} [_ id title completed]]
   (js/console.debug "try to update" id title completed)
   {:db db
    :http-xhrio
    {:method :post
     :uri "http://localhost:3022/todo/update"
     :params {:id id :title title :completed completed}
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::todo-http-result]
     :on-failure [::todo-http-error]}}))

(r/reg-event-db
 ::todo-new-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db :todo-list [])))

(r/reg-event-db
 ::todo-new-success
 (fn [db [_ result]]
   (js/console.debug "current db : " db)
   (assoc db :todo-list result)))

(r/reg-event-fx
 ::new
 (fn [{:keys [db]} [_ id title completed]]
   (js/console.debug "add to new" id title completed)
   {:db db
    :http-xhrio
    {:method :post
     :uri "http://localhost:3022/todo/new"
     :params {:id id :title title :completed completed}
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::todo-new-success]
     :on-failure [::todo-new-error]}}))

(comment 
  
  (ajax/POST ""))