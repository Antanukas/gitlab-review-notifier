(ns gitlab-review-notifier.core
  (:gen-class)
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [gitlab-review-notifier.predicates :as pred]
            [overtone.at-at :as scheduler]
            [clj-time.core :as t]))

;atom to check for new mrs based on previous
(def prev-merge-request-ids (atom #{}))


(defn get-merge-requests []
  (->> (gitlab/get-projects)
       (map :id)
       (map gitlab/get-open-merge-requests)
       (flatten)))

(defn play-file! [config-key]
  (player/play-file! (config-key @ctx/config) true))

(defn remember-merge-requests! [merge-requests]
  (reset! prev-merge-request-ids (set (map :id merge-requests))))

(defn check-for-new-mrs! []
  (let [merge-requests (get-merge-requests)
        new-merge-requests (filter (pred/is-new-mr? @prev-merge-request-ids) merge-requests)]
    (if-not (empty? new-merge-requests) (play-file! :new-review-sound))
    (remember-merge-requests! merge-requests)))


;(scheduler/every 10000 #(player/play-file (:new-review-sound config/@config) true) scheduler-pool)
(defn- to-milis [seconds] (* seconds 1000))
(defn -main
  [& args]
  (println "Starting application...")
  (println "Rememberig current merge requests")
  (remember-merge-requests! (get-merge-requests))
  (println "Scheduling config reload every 10 seconds")
  (scheduler/every (to-milis (:config-reload-interval-sec @ctx/config)) ctx/load-config ctx/scheduler-pool)
  (println "Scheduling new review checking task")
  (scheduler/every (to-milis (:new-review-check-interval-sec @ctx/config))
                   check-for-new-mrs!
                   ctx/scheduler-pool))
