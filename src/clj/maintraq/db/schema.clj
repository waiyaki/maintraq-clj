(ns maintraq.db.schema
  (:require
   [clojure.string :as str]
   [io.rkn.conformity :as conformity]
   [maintraq.db.partition]
   [maintraq.db.schema.user]
   [taoensso.timbre :as timbre]))


(defn schema?
  "For a given var `v`, determine if the var is a schema or not, indicated by
  the presence of a `:schema` meta key."
  [v]
  (some? (:schema (meta v))))


(defn schema-ns?
  "Whether the given namespace is a schema namespace."
  [ns]
  (-> ns ns-name str (str/starts-with? "maintraq.db.schema.")))


(defn find-schema
  "Given namespaces, find schema namespaces and return the schema vars from those namespaces."
  ([ns]
   (find-schema ns schema-ns?))
  ([ns schema-ns?]
   (->> ns
        (filter schema-ns?)
        (reduce (fn [m ns] (merge m (ns-publics ns))) {})
        (vals)
        (filter schema?))))


(defn norms-map
  "Construct a norms-map expected by conformity from a sequence of schema vars."
  [schema]
  (reduce
   (fn [m s]
     (let [metadata (meta s)]
       (assoc m
              (:schema metadata)
              (if (some? (:requires metadata))
                {:requires (:requires metadata)
                 :txes     [(var-get s)]}
                {:txes [(var-get s)]}))))
   {}
   schema))


(defn partition-norms
  "Create norms for application's custom db partitions from the given namespaces"
  []
  (norms-map (find-schema [(the-ns 'maintraq.db.partition)] (constantly true))))


(defn install-partitions! [conn]
  (timbre/info "Installing partitions...")
  (conformity/ensure-conforms conn (partition-norms)))


(defn install-schema! [conn]
  (timbre/info "Installing schema...")
  (conformity/ensure-conforms conn (norms-map (find-schema (all-ns)))))


(defn conform-schema! [conn]
  (install-partitions! conn)
  (install-schema! conn))
