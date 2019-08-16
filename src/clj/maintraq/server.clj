(ns maintraq.server
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [taoensso.timbre :as timbre]))


(defn start [{:keys [handler port] :as opts}]
  (try
    (let [server (run-jetty
                   handler
                   (-> opts
                     (dissoc :handler)
                     (assoc :join? false)))]
      (timbre/info "Server started on port" port)
      server)
    (catch Throwable t
      (timbre/error t (str "Server failed to start on port: " port)))))


(defn stop [server]
  (.stop server)
  (timbre/info "HTTP server stopped."))
