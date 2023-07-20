(ns todo.sandbox
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as ring]
            [mount.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.coercion :as coercion]))

(def app
  (ring/ring-handler
   (ring/router
    ["/"
     #_["mirror" {:get (fn [req]
                         (resp/response (str req)))}]
     #_["mirror/:id" {:get (fn [req]
                             (resp/response (str req)))}]
     #_todo/routes]
    {:data  
     {:coercion   reitit.coercion.spec/coercion
      :muuntaja   m/instance
      :middleware []}}
    #_{:data {:middleware [wrap-reload
                         #_params/wrap-params
                         #_muuntaja/format-middleware
                         #_coercion/coerce-exceptions-middleware
                         #_coercion/coerce-request-middleware
                         #_coercion/coerce-response-middleware]}})
   #_(ring/create-default-handler)))


