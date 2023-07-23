(ns todo.todo
  (:require [reagent.core :as r]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [ajax.core :as ajax]))

(rf/reg-event-db
 ::todo-load-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db :todo-list [])))

(rf/reg-event-db
 ::todo-load-result
 (fn [db [_ result]] 
   (assoc db :todo-list result)))

(rf/reg-event-fx
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

(rf/reg-sub
 :todo 
 (fn [db]
   (js/console.debug "subscribe db : "
                     (:todo-list db))
   (:todo-list db)))

(rf/reg-event-db
 ::todo-update-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db :todo-list [])))

(rf/reg-event-db
 ::todo-update-result
 (fn [db [_ result]]
   (assoc db :todo-list result)))

(rf/reg-event-fx
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
     :on-success [::todo-update-result]
     :on-failure [::todo-update-error]}}))

(rf/reg-event-db
 ::todo-new-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db :todo-list [])))

(rf/reg-event-db
 ::todo-new-success
 (fn [db [_ result]] 
   (assoc db :todo-list result)))

(rf/reg-event-fx
 ::new
 (fn [{:keys [db]} [_ title]]
   (js/console.debug "add to new :" title)
   {:db db
    :http-xhrio
    {:method :post
     :uri "http://localhost:3022/todo/new"
     :params {:title title}
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::todo-new-success]
     :on-failure [::todo-new-error]}}))

(defn todo-item
  [ul {:keys [id title completed]}]
  (let [style {true {:color  "red"
                     :text-decoration "line-through"}
               false {:color "black"}}]
    (conj ul
          [:li
           {:style (style completed)
            :on-click
            (fn [_]
              (rf/dispatch
               [:todo.todo/update id title (not completed)])
              (rf/dispatch [:todo.todo/load nil]))}
           title])))


(defn todo-list
  []
  (rf/dispatch [:todo.todo/load nil])
  (fn []
    (let [sample-list @(rf/subscribe [:todo])]
      (reduce todo-item [:ul] sample-list))))

(defn todo-new
  []
  (let [title (r/atom "")]
    (fn []
      [:div "New Todo : "
       [:input {:type "text"
                :on-change
                (fn [e]
                  (r/rswap! title
                            (fn []
                              (-> e .-target .-value))))}]
       [:input {:type "button" :value "submit"
                :on-click
                (fn [_e]
                  (rf/dispatch
                   [:todo.todo/new @title])
                  (js/console.log "clicked"))}]])))

(defn todo-component
  []
  (fn []
    [:div {:id "todo-component"}
     [:p "TODO list : "]
     ((todo-list))
     #_(todo-new)]))


(comment 
  
  (ajax/POST ""))