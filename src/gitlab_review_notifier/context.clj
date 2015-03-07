(ns gitlab-review-notifier.context
  (:require [overtone.at-at :as scheduler]
            [clojure.edn :as edn]))

;pool used for scheduling tasks. E.g checking for new merge requests
(def scheduler-pool (scheduler/mk-pool))

(def config-file
  (or (System/getProperty "config.location") "resources/config.edn"))

(defn load-config [] (edn/read-string (slurp config-file)))

(def config (atom (load-config)))



