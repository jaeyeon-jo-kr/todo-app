(ns todo.core
  (:require [reagent.dom :as re-dom]
            [todo.todo :as t]))



(defn main-component
  [] 
  [:div {:id "main-comp"}
   ((t/todo-component))])



(defn ^:export init
  [& _params]
  (re-dom/render
   [main-component]
   (.getElementById js/document "app")))

(comment
  
  )