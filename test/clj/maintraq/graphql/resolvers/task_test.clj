(ns maintraq.graphql.resolvers.task-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all]
   [maintraq.deps :as deps]
   [maintraq.seed.tasks :as seed.tasks]
   [maintraq.seed.users :as seed.users]
   [maintraq.test-utils.core :as tutils]
   [maintraq.test-utils.graphql.core :as gql]))


(use-fixtures :each (tutils/server-fixture))


(deftest create-task-test
  (let [{:keys [conn]} (deps/deps)
        member         (seed.users/user! conn (seed.users/user {:password "password"}))
        token          (gql/login! {:username (:user/username member) :password "password"})
        mutation*      (fn [input]
                         (gql/mutation {:queries [[:task_create
                                                   {:input input}
                                                   [:title :description]]]}
                                       {:headers {"Authorization" (str "Bearer " token)}}))]
    (testing "validates task title and description"
      (let [res (mutation* {:description "short" :title ""})]
        (is (= 400 (:status res)))
        (is (= (get-in res [:body :errors 0 :extensions :errors :title])
               "this field is mandatory"))
        (is (= (get-in res [:body :errors 0 :extensions :errors :description])
               "less than the minimum 10"))))

    (testing "successfully creates a task"
      (let [data {:description "Some description" :title "Some task"}
            res  (mutation* data)]
        (is (= 200 (:status res)))
        (is (= data (-> res :body :data :task_create)))))))


(deftest update-task-test
  (let [{:keys [conn]} (deps/deps)
        member         (seed.users/user! conn (seed.users/user {:password "password"}))
        admin          (seed.users/user! conn (seed.users/admin {:password "password"}))
        maintenance    (seed.users/user!
                        conn
                        (seed.users/user {:role :user.role/maintenance :password "password"}))
        task           (seed.tasks/task!
                        conn
                        (seed.tasks/task {:title       "Some task"
                                          :description "Task description"
                                          :requester   (:db/id member)}))
        member-token   (gql/login! {:username (:user/username member) :password "password"})
        mutation*      (fn [input & [t fields]]
                         (gql/mutation {:queries [[:task_update
                                                   {:input input}
                                                   (into [:title :description] fields)]]}
                                       {:headers {"Authorization" (str "Bearer " (or t member-token))}}))]
    (testing "validates task title and description"
      (let [res (mutation* {:uid (str (:task/uid task)) :params {:description "short" :title ""}})]
        (is (= 400 (:status res)))
        (is (= (get-in res [:body :errors 0 :extensions :errors :title])
               "less than the minimum 1"))
        (is (= (get-in res [:body :errors 0 :extensions :errors :description])
               "less than the minimum 10"))))

    (testing "task updates by regular members"
      (testing "members successfully updates a task's title and description"
        (let [data {:uid    (str (:task/uid task))
                    :params {:description "Some updated description" :title "Some updated title"}}
              res  (mutation* data)]
          (is (= 200 (:status res)))
          (is (= (:params data) (-> res :body :data :task_update)))))

      (testing "members can only update a task's title and description"
        (let [data {:uid    (str (:task/uid task))
                    :params {:assignee (str (:user/uid maintenance))}}
              res  (mutation* data)]
          (is (= 400 (:status res)))
          (is (= "Members can only update a task's title or description."
                 (-> res :body :errors first :message))))))

    (testing "task updates by maintenance crew members"
      (let [token (gql/login! {:username (:user/username maintenance) :password "password"})]
        (testing "successfully updates a task"
          (let [data {:uid    (str (:task/uid task))
                      :params {:description "Some updated description"
                               :title       "Some updated title"
                               :status      'confirmed}}
                res  (mutation* data token [:status])]
            (is (= 200 (:status res)))
            (is (= (assoc (:params data) :status "confirmed")
                   (-> res :body :data :task_update)))))

        (testing "cannot change assignee"
          (let [data {:uid    (str (:task/uid task))
                      :params {:assignee (str (:user/uid maintenance))}}
                res  (mutation* data token [[:assignee [:uid]]])]
            (is (= 400 (:status res)))
            (is (= "Maintenance crew members cannot change a task's assignee."
                   (-> res :body :errors first :message)))))))

    (testing "task updates by admin members"
      (let [token (gql/login! {:username (:user/username admin) :password "password"})
            data  {:uid    (str (:task/uid task))
                   :params {:assignee    (str (:user/uid maintenance))
                            :title       "Admin updated title"
                            :description "Admin updated description"
                            :status      'done}}
            res   (mutation* data token [:status [:assignee [:uid]]])]
        (is (= (-> res :body :data :task_update)
             (assoc (:params data)
                      :status "done"
                      :assignee {:uid (str (:user/uid maintenance))})))

        (testing "cannot assign a task to a non-maintenance user"
          (let [data {:uid    (str (:task/uid task))
                      :params {:assignee (str (:user/uid member))}}
                res   (mutation* data token [:status [:assignee [:uid]]])]
            (is (= 400 (:status res)))
            (is (= "Invalid assignee. Tasks can be assigned to maintenance users only."
                   (-> res :body :errors first :message)))))))))
