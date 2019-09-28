(ns maintraq.graphql.resolvers.user-test
  (:require
   [clojure.test :refer :all]
   [maintraq.test-utils.core :as tut.core]
   [maintraq.test-utils.graphql.core :as tut.graphql]))


(use-fixtures :each (tut.core/server-fixture))


(deftest user-creation
  (let [email-params (atom nil)]
    (with-redefs [maintraq.services.mailgun.core/send-email!
                  (fn [_ params]
                    (swap! email-params assoc :params params))]
      (let [data {:username         "test"
                  :email            "test@user.com"
                  :password         "test_password"
                  :confirm_password "test_password"}
            res  (tut.graphql/mutation
                  {:queries [[:user_create {:input data}
                              [:email :username :activated :role]]]})]
        (testing "Creates a new account"
          (is (= (get-in res [:data :user_create])
                 {:activated false
                  :role      "member"
                  :username  "test"
                  :email     "test@user.com"})))

        (testing "Sends an email to the user"
          (is (= "test@user.com" (get-in @email-params [:params :to])))
          (is (= "Confirm Your Email" (get-in @email-params [:params :subject]))))))))


(deftest user-creation-params-validation
  (testing "validates user creation parameters"
    (let [mutation*    (fn [data]
                         (tut.graphql/mutation
                          {:queries [[:user_create {:input data}
                                      [:email :username :activated :role]]]}))
          data         {:username         "test"
                        :email            "test@user.com"
                        :password         "test_password"
                        :confirm_password "test_password"}]
      (testing "validates password lengths"
        (let [res   (mutation* (assoc data :password "short"))
              error (-> res :errors first :extensions :errors :password)]
          (is (= error "less than the minimum 8"))))

      (testing "validates matching passwords"
        (let [res   (mutation* (assoc data :confirm_password "not-a-match"))
              error (-> res :errors first :extensions :errors :confirm_password)]
          (is (= error "does not match"))))

      (testing "validates emails"
        (let [res   (mutation* (assoc data :email "invalid"))
              error (-> res :errors first :extensions :errors :email)]
          (is (= error "must be a valid email"))))

      (with-redefs [maintraq.services.mailgun.core/send-email! (constantly nil)]
        (testing "validates uniqueness of emails and usernames"
          (let [_     (mutation* data)
                res   (mutation* data)
                errors (-> res :errors first :extensions :errors)]
            (is (= (:username errors) "is unavailable"))
            (is (= (:email errors) "is unavailable"))))))))
