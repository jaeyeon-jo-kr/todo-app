(ns todo.color
  (:require
   [reagent.core :as r]
   [day8.re-frame.http-fx]
   [re-frame.core :as rf]
   [ajax.core :as ajax]))

(rf/reg-event-db
 ::load-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db ::list [])))

(rf/reg-event-db
 ::load-result
 (fn [db [_ result]]
   (assoc db ::list result)))

(rf/reg-event-fx
 ::get
 (fn [{:keys [db]} _]
   {:db db
    :http-xhrio
    {:method :get
     :uri "http://localhost:3022/color/all"
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::load-result]
     :on-failure [::load-error]}}))

(rf/reg-sub
 ::subscribe
 (fn [db]
   (::list db)))

(rf/reg-event-db
 ::update-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db ::list [])))

(rf/reg-event-db
 ::update-result
 (fn [db [_ result]]
   (assoc db ::list result)))

(rf/reg-event-fx
 ::update
 (fn [{:keys [db]} [_ id title completed]]
   (js/console.debug "try to update" id title completed)
   {:db db
    :http-xhrio
    {:method :post
     :uri "http://localhost:3022/color/update"
     :params {:id id :title title :completed completed}
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::color-update-result]
     :on-failure [::color-update-error]}}))

(rf/reg-event-db
 ::new-error
 (fn [db [_ result]]
   (js/console.error "error : " result)
   (assoc db ::list [])))

(rf/reg-event-db
 ::tnew-success
 (fn [db [_ result]]
   (assoc db ::list result)))

(rf/reg-event-fx
 ::new
 (fn [{:keys [db]} [_ title]]
   (js/console.debug "add to new :" title)
   {:db db
    :http-xhrio
    {:method :post
     :uri "http://localhost:3022/color/new"
     :params {:title title}
     :timeout 8000
     :format          (ajax/json-request-format)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success [::color-new-success]
     :on-failure [::color-new-error]}}))

(defn items
  []
  (let [coll @(rf/subscribe [::subscribe])]
    

    
    
    ))



(defn component
  []
  (fn []
    (rf/dispatch [::get])
    [:div {:id "color-component"}
     [:p "Color list : "]
     (list)
     #_(color-new)]))



