(defproject maintraq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "0.4.2"]
                 [aero "1.1.3"]
                 [buddy/buddy-hashers "1.4.0"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.walmartlabs/lacinia "0.34.0"]
                 [datomic-schema "1.3.0"]
                 [funcool/struct "1.3.0"]
                 [io.rkn/conformity "0.5.1"]
                 [mount "0.1.16"]
                 [metosin/reitit "0.3.9"]
                 [metosin/muuntaja "0.6.4"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-logger "1.0.1"]]

  :min-lein-version "2.0.0"

  :main ^:skip-aot maintraq.core
  :target-path "target/%s"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :profiles {:uberjar {:aot :all
                       :source-paths ["env/prod/clj"]}
             :dev     {:source-paths ["env/dev/clj"]
                       :repl-options {:init-ns user}
                       :dependencies [[org.clojure/tools.namespace "0.3.1"]
                                      [expound "0.7.2"]
                                      [ring/ring-devel "1.7.1"]
                                      [faker "0.2.2"]]}})
