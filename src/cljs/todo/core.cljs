(ns todo.core
  (:require [reagent.dom :as re-dom] 
            [reagent.core :as r]
            [re-frame.core :as rf]
            [todo.todo]))

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
                  (js/console.log "clicked"))}]]))
  )

(comment 
  ((todo-list)))

(defn todo-component
  []
  (fn []
    [:div
     [:p "TODO list : "]
     ((todo-list))
     ((todo-new))]))


(defn ^:export init
  [& _params]
  (re-dom/render
   [todo-component]
   (.getElementById js/document "app")))


(comment 
  
  )