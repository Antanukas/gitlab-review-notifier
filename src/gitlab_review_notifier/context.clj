(ns gitlab-review-notifier.context
  (:require [overtone.at-at :as scheduler]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

;pool used for scheduling tasks. E.g checking for new merge requests
(def scheduler-pool (scheduler/mk-pool))

(def config-file
  (or (io/file (System/getProperty "config.location")) (io/resource "config.edn")))

(def config (atom {}))

(defn load-config []
  (let [cfg (edn/read-string (slurp config-file))]
    (reset! config cfg)))



