(ns maintraq.test-utils.core
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [maintraq.core :as maintraq]
   [mount.core :as mount]
   [taoensso.timbre :as timbre]))


(defn- start-app [args]
  (let [{:keys [options errors]} (cli/parse-opts args maintraq/cli-options)]
    (when errors
      (timbre/error (str/join "/n" errors))
      (System/exit 1))
    (mount/start-with-args {:env (:env options)})
    (timbre/info "Application started!")))


(defn server-fixture []
  (fn [test-fn]
    (start-app ["-e" "test"])
    (test-fn)
    (mount/stop)
    (timbre/info "Application stopped!")))
