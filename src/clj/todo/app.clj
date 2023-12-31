(ns todo.app
  (:require 
   [clojure.tools.logging :as log]
   [ring.middleware.params :as params]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [muuntaja.core :as mc]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec]
   [reitit.ring :as ring]
   [reitit.interceptor.sieppari]
   [ring.util.response :as res] 
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.adapter.jetty :as jetty]
   [todo.states :refer [server]]
   [todo.db]
   [todo.todo :as todo]
   [todo.color :as color]))

(defn wrap-cors
  [handler]
  (fn [request]
    ((comp
      #(res/header %
                   "Access-Control-Allow-Methods"
                   "GET,HEAD,OPTIONS,POST,PUT")
      #(res/header %
                   "Access-Control-Allow-Headers"
                   "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Authorization")
      #(res/header % "Access-Control-Allow-Origin" "*")
      #(res/header % "Access-Control-Max-Age" "86400")
      handler)
     request)))


(def app
  (ring/ring-handler
   (ring/router
    [""
     ["/mirror" {:get (fn [req]
                        (res/response (str req)))
                 :post (fn [req]
                         (res/response
                          (str
                           (select-keys
                            req
                            [:body-params :body :params :path-params]))))}]
     ["/mirror/:id" {:get (fn [req]
                            (res/response (str req)))}]
     todo/routes
     color/routes]
    {:data {:coercion   reitit.coercion.spec/coercion
            :muuntaja   muuntaja.core/instance
            :middleware [wrap-cors
                         coercion/coerce-exceptions-middleware
                         coercion/coerce-response-middleware
                         muuntaja/format-middleware
                         params/wrap-params
                         coercion/coerce-request-middleware]}})
   (ring/create-default-handler)))




(defn start!
  []
  (reset! server (jetty/run-jetty #'app 
                  {:port 3022 :join? false})))

(defn stop!
  []
  (swap! server #(.stop %)))

(comment
  (start!)
  (stop!)
  
  (app {:origin "http://localhost:808"
        :request-method :get
        :uri "/todo/get/1"})
  (app {:request-method :get :uri "/todo/all"})

  (app {:request-method :get :uri "/todo/all"})

  (app {:request-method :post
        :uri "/todo/new"
        :muuntaja/request {:format "application/edn"}
        :body-params {:id 4
                      :title "생수 사기"
                      :completed true}})

  (app {:request-method :post
        :uri "/todo/update"
        :muuntaja/request {:format "application/edn"}
        :body-params {:id 4
                      :title "생수 사기"
                      :completed true}})
  (app {:request-method :post
        :uri "/mirror"
        :muuntaja/request {:format "application/edn"}
        :body-params {:id 4
                      :title "생수 사기"
                      :completed true}})
  (app {:request-method :get :uri "/mirror/1"})

  (app {:request-method :get :uri "/1"})
  (require  '[ns-tracker.core :as core])

  (def modified-namespaces
    (core/ns-tracker ["src"])))

