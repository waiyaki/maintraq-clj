(ns maintraq.validation.schema
  (:require
   [struct.core :as st]
   [maintraq.validation.core :as validation :refer [unique]]))


(defn user [db]
  {:username         [st/required st/string (unique db :user/username)]
   :email            [st/required st/email (unique db :user/email)]
   :password         [st/required st/string [st/min-count 8]]
   :confirm_password [st/required st/string [st/min-count 8] [st/identical-to :password]]
   :first_name       [st/string]
   :last_name        [st/string]
   :middle_name      [st/string]})


(defn facility [db]
  {:name [st/required st/string (unique db :facility/name) [st/min-count 1]]})
