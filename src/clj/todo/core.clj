(ns todo.core
  (:require 
   [clojure.tools.logging :as log] 
   [todo.db :as db]
   [todo.app :as app]
   [todo.color]
   [todo.todo]))

(defn start!
  []
  (db/start!)
  (app/start!))

(defn stop!
  []
  (db/stop!)
  (app/stop!))


(defn main
  [& params]
  (log/info "start server")
  (start!))

(comment
  (start!)

  )



