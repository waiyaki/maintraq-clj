(ns maintraq.core
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [mount.core :as mount :refer [defstate]]
   [maintraq.deps :as deps]
   [maintraq.server :as server]
   [maintraq.handler :as handler]
   [maintraq.config :as config :refer [config]]
   [maintraq.db.core]
   [taoensso.timbre :as timbre]))


(defstate ^{:on-reload :noop} http-server
  :start (server/start {:handler (handler/app (deps/deps))
                        :port    (config/server-port config)})
  :stop (when http-server
          (server/stop http-server)))


(def cli-options
  [["-e" "--environment ENVIRONMENT" "The environment to start the server in."
    :id :env
    :default :prod
    :parse-fn keyword
    :validate [#{:prod :dev :test} "Must be one of #{:prod :dev}"]]])


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (timbre/info "stopped" component))
  (shutdown-agents))


(defn start-app [args]
  (let [{:keys [options errors]} (cli/parse-opts args cli-options)]
    (when errors
      (timbre/error (str/join "/n" errors))
      (System/exit 1))
    (doseq [component (:started (mount/start-with-args {:env (:env options)}))]
      (timbre/info "started" component))
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))))


(defn -main [& args]
  (start-app args))
