(ns maintraq.services.mailgun.emails
  (:require
   [maintraq.config :as config]))


(defn confirm-registration [config user]
  (format "
    Dear %s,

    Welcome to MainTraq!

    To confirm your account please click on the following link:

    %s

    Sincerely,

    The MainTraq Team

    Note: replies to this email address are not monitored."
          (:user/username user)
          (format "%s/auth/confirm/%s" (config/host config :api) (:user/activation-hash user))))
