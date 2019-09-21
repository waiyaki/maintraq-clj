(ns maintraq.services.mailgun.core
  (:require
   [clj-http.client :as client]
   [maintraq.config :as config]
   [taoensso.timbre :as timbre]))


(defn send-email!
  "Send an email via the Mailgun API."
  [config {:keys [from to subject text] :as data}]
  (let [api-key (config/mailgun-api-key config)
        domain  (config/mailgun-domain config)]
    (client/post (format "%s/messages" domain)
                 {:async?      true
                  :basic-auth  ["api" api-key]
                  :form-params {:from    (or from "noreply@maintraq.com")
                                :to      to
                                :subject subject
                                :text    text}
                  :as          :x-www-form-urlencoded
                  :accept      :json}
                 (fn [response]
                   (timbre/info ::email-schedule-success (:body response)))
                 (fn [e]
                   (timbre/error e "Email sending error")))))
