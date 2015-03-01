(ns gitlab-review-notifier.config)

(def config-file
  (or (System/getProperty "config.location") "resources/config.edn"))

(require 'clojure.edn)
(def config (clojure.edn/read-string (slurp config-file)))


