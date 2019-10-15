(ns maintraq.graphql.resolvers.facility-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all]
   [maintraq.deps :as deps]
   [maintraq.seed.users :as seed.users]
   [maintraq.test-utils.core :as tutils]
   [maintraq.test-utils.graphql.core :as gql]))


(use-fixtures :once (tutils/server-fixture))


(deftest create-facility-test
  (testing "successfully creates a facility"
    (let [{:keys [conn]} (deps/deps)
          admin          (seed.users/user! conn (seed.users/admin {:password "password"}))
          token          (gql/login! {:username (:user/username admin) :password "password"})
          input          {:name "Facility A"}
          res            (gql/mutation
                          {:queries [[:facility_create
                                      {:input input}
                                      [:name :uid]]]}
                          {:headers {"Authorization" (str "Bearer " token)}})]
      (is (= 200 (:status res)))
      (is (= (:name input) (get-in res [:body :data :facility_create :name])))

      (testing "creates an audit trail when a facility is created"
        (let [facility-uid (java.util.UUID/fromString
                            (get-in res [:body :data :facility_create :uid]))
              audit-user   (d/q '[:find ?audit-user .
                                  :in $ ?uid
                                  :where
                                  [?f :facility/uid ?uid ?tx]
                                  [?tx :audit/user ?audit-user]]
                                (d/db conn) facility-uid)]
          (is (= audit-user (:db/id admin)))))

      (testing "validates unique facility names"
        (let [res (gql/mutation
                   {:queries [[:facility_create
                               {:input input}
                               [:name :uid]]]}
                   {:headers {"Authorization" (str "Bearer " token)}})]
          (is (= 400 (:status res)))
          (is (= "Validation error" (get-in res [:body :errors 0 :message])))
          (is (= "is unavailable" (get-in res [:body :errors 0 :extensions :errors :name])))))

      (testing "restricts access from non-admin accounts"
        (let [user       (seed.users/user! conn (seed.users/user {:password "password"}))
              user-token (gql/login! {:username (:user/username user) :password "password"})
              res        (gql/mutation
                          {:queries [[:facility_create
                                      {:input {:name "Other"}}
                                      [:name :uid]]]}
                          {:headers {"Authorization" (str "Bearer " user-token)}})]
          (is (= 403 (:status res)))
          (is (= "Not authorized to access these resources." (get-in res [:body :errors 0 :message]))))))))
