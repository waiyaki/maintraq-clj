(ns maintraq.auth.resolvers
  (:require
   [datomic.api :as d]
   [maintraq.handlers.errors :as errors]
   [maintraq.db.models.user :as user]))


(defn admin? [{:keys [requester]} _ _]
  (user/admin? requester))


(defn maintenance? [{:keys [requester]} _ _]
  (user/maintenance? requester))


(defn member? [{:keys [requester]} _ _]
  (user/member? requester))


(defn authenticated? [{:keys [requester]} _ _]
  (some? requester))


(defn authorize
  "Wrap a `resolver` fn with a resolver which checks if the user in the
  incoming request is authorized to access the requested resources.

  Authorization is specified in the metadata of `resolver`, provided as
  `resolver-meta`, under the `:authorized?` meta key.

  `:authorized?` can be a single function, or a vector of functions. When a vector
  of functions is specified, access is granted if any of the functions returns `true`.
  The `:authorized?` functions are invoked with a three arguments: a context
  containing a db value and the requester, the request args and the resolved value.

  `:authorized?` functions have to explicitly return `true`. Any other value is
  denied access (truthy or not).

  When the `resolver` does not explicitly define the `:authorized?` meta key,
  access is constantly denied by default.

  `resolver`'s metadata can additionally optionally contain:
    - an `:allow-admin?` flag. By default, admin accounts will be granted access
    to every resolver authorized with this function. When any value other than
    explicit `true` is provided for `:allow-admin?`, admin accounts are not allowed access.
    - an `:authorization-error` key. This is the error to resolve with when the
    authorization fails. Defaults to a 403."
  [resolver resolver-meta]
  (fn [{:keys [conn requester] :as ctx} args value]
    (let [{:keys [authorized? allow-admin? authorization-error]
           :or   {authorized?         (constantly false)
                  allow-admin?        true
                  authorization-error (errors/forbidden "Not authorized to access these resources.")}}
          resolver-meta
          auth-ctx          {:db        (d/db conn)
                             :requester requester}
          authorization-fns (cond-> authorized?
                              (fn? authorized?)    vector
                              (true? allow-admin?) (conj admin?))
          can-access?       (some true? (map #(apply % [auth-ctx args value])
                                             authorization-fns))]
      (if can-access?
        (resolver ctx args value)
        authorization-error))))


(defn authorize-resolvers
  "Accept a var of `resolvers` and wrap each resolver function in the map bound
  to the `resolvers` var with the `authorize` resolver. Return a map of the wrapped resolvers.

  See more in the `authorize` resolver docstring."
  [resolvers]
  (let [ns               (:ns (meta resolvers))
        ns-vars-by-value (into {} (for [[_ value] (ns-publics ns)
                                        :when     (var? value)]
                                    [(var-get value) value]))
        resolvers-map    (var-get resolvers)]
    (into resolvers-map
          (for [[resolver-name resolver-fn] resolvers-map]
            [resolver-name
             (authorize resolver-fn
                        (meta (get ns-vars-by-value resolver-fn)))]))))
