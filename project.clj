(defproject maintraq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 ;; Config
                 [aero "1.1.3"]
                 ;; Reloaded workflow
                 [mount "0.1.16"]]

  :min-lein-version "2.0.0"

  :main ^:skip-aot maintraq.core
  :target-path "target/%s"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths ["env/dev/clj"]
                       :repl-options {:init-ns user}
                       :dependencies [[org.clojure/tools.namespace "0.3.1"]
                                      [expound "0.7.2"]]}})
