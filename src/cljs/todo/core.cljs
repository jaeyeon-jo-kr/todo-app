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


(defn list-component
  []
  (rf/dispatch [:todo.todo/load nil])
  (fn []
    (let [sample-list @(rf/subscribe [:todo])]
      (reduce todo-item [:ul] sample-list))))

(defn ^:export init
  [& _params]
  (re-dom/render
   [list-component]
   (.getElementById js/document "app")))


(comment 
  
  )