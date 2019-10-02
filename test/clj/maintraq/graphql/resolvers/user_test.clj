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


(deftest user-login-test
  (let [{:keys [conn]} (deps/deps)
        login*         (fn [creds]
                         (tut.graphql/mutation
                          {:queries [[:user_login {:input creds}
                                      [:token]]]}))
        creds          {:password "password" :username "test"}
        user           (seed.users/user! conn (seed.users/user creds))]
    (testing "with valid credentials, successfully logs a user in."
      (let [res (login* creds)]
        (is (= 200 (:status res)))
        (is (string? (-> res :body :data :user_login :token)))))

    (testing "handles invalid usernames."
      (let [res (login* (assoc creds :username "invalid"))]
        (is (= 401 (:status res)))
        (is (= "Invalid username/password combination."
               (-> res :body :errors first :message)))))

    (testing "handles invalid passwords."
      (let [res (login* (assoc creds :password "invalid"))]
        (is (= 401 (:status res)))
        (is (= "Invalid username/password combination."
               (-> res :body :errors first :message)))))

    (testing "does not log inactive users in."
      (let [inactive-creds {:username  "inactive-test"
                            :password  "password"
                            :activated false}
            inactive-user  (seed.users/user! conn (seed.users/user inactive-creds))
            res            (login* (dissoc inactive-creds :activated))]
        (is (= 403 (:status res)))
        (is (= "This user account is inactive."
               (-> res :body :errors first :message)))))))


(deftest user-query-test
  (let [{:keys [conn]} (deps/deps)
        creds          {:username "test" :password "password"}
        user           (seed.users/user! conn (seed.users/user creds))
        token*         (fn [input]
                         (get-in
                          (tut.graphql/mutation
                           {:queries [[:user_login {:input input}
                                       [:token]]]})
                          [:body :data :user_login :token]))
        token          (token* creds)
        query*         (fn [input & [t]]
                         (tut.graphql/query
                          {:queries [[:user {:input input}
                                      [:uid :username :full_name]]]}
                          {:headers {"Authorization" (str "Bearer " (or t token))}}))]
    (testing "retrieves user details"
      (let [res (query* {:username (:user/username user)})]
        (is (= "test" (-> res :body :data :user :username)))
        (is (= (str (:user/uid user))
               (-> res :body :data :user :uid)))
        (is (= (str (:user/first-name user) " " (:user/last-name user))
               (-> res :body :data :user :full_name)))))

    (testing "does not allow users to access details of other users"
      (let [other-creds {:username "other" :password "other-password"}
            other-user  (seed.users/user! conn (seed.users/user other-creds))
            other-token (token* other-creds)
            res         (query* {:username (:user/username user)} other-token)]
        (is (= 404 (:status res)))
        (is (= "User not found."
               (-> res :body :errors first :message)))))

    (testing "allows admins access to other user accounts"
      (let [admin       (seed.users/user! conn (seed.users/admin {:password "password"}))
            admin-token (token* {:username "admin" :password "password"})
            res         (query* {:username (:user/username user)} admin-token)]
        (is (= "test" (-> res :body :data :user :username)))
        (is (= (str (:user/uid user))
               (-> res :body :data :user :uid)))

        (testing "handles missing users"
          (let [res (query* {:username "missing"} admin-token)]
            (is (= 404 (:status res)))
            (is (= "User not found."
                   (-> res :body :errors first :message)))))))))
