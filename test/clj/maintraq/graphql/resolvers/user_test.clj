(ns maintraq.graphql.resolvers.user-test
  (:require
   [clojure.test :refer :all]
   [datomic.api :as d]
   [maintraq.deps :as deps]
   [maintraq.seed.users :as seed.users]
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
          (is (= (get-in res [:body :data :user_create])
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
              error (-> res :body :errors first :extensions :errors :password)]
          (is (= error "less than the minimum 8"))
          (is (= 400 (:status res)))))

      (testing "validates matching passwords"
        (let [res   (mutation* (assoc data :confirm_password "not-a-match"))
              error (-> res :body :errors first :extensions :errors :confirm_password)]
          (is (= error "does not match"))
          (is (= 400 (:status res)))))

      (testing "validates emails"
        (let [res   (mutation* (assoc data :email "invalid"))
              error (-> res :body :errors first :extensions :errors :email)]
          (is (= error "must be a valid email"))
          (is (= 400 (:status res)))))

      (with-redefs [maintraq.services.mailgun.core/send-email! (constantly nil)]
        (testing "validates uniqueness of emails and usernames"
          (let [_     (mutation* data)
                res   (mutation* data)
                errors (-> res :body :errors first :extensions :errors)]
            (is (= (:username errors) "is unavailable"))
            (is (= (:email errors) "is unavailable"))
            (is (= 400 (:status res)))))))))


(deftest user-activation-test
  (let [{:keys [conn]} (deps/deps)
        activate*      (fn [input]
                         (tut.graphql/mutation
                          {:queries [[:user_activate {:input input}
                                      [:activated]]]}))
        tx-user        (seed.users/user {:activated false})
        user           (seed.users/user! conn tx-user)
        input          {:uid             (str (:user/uid user))
                        :activation_hash (str (:user/activation-hash user))}
        res            (activate* input)]
    (is (true? (-> res :body :data :user_activate :activated)))

    (testing "conflicts when the user is already active"
      (let [res   (activate* input)
            error (-> res :body :errors first :message)]
        (is (= 409 (:status res)))
        (is (= error "Account is already active."))))

    (testing "errors out for invalid user/activation-hash combination"
      (let [invalid-hash (activate* (assoc input :activation_hash (str (java.util.UUID/randomUUID))))
            invalid-uid  (activate* (assoc input :uid (str (java.util.UUID/randomUUID))))]
        (is (= 400 (:status invalid-hash)))
        (is (= 400 (:status invalid-uid)))
        (is (= (-> invalid-hash :body :errors first :message)
               "Invalid user/hash combination."))
        (is (= (-> invalid-uid :body :errors first :message)
               "Invalid user/hash combination."))))))
