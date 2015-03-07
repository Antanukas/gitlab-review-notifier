(ns gitlab-review-notifier.core
  (:gen-class)
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [gitlab-review-notifier.predicates :as pred]
            [overtone.at-at :as scheduler]
            [clj-time.core :as t]
            [taoensso.timbre :as timbre]))

;convinience aliases for timbre logging library
(timbre/refer-timbre)

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
    (info "Checking for new merge requests")
    (if-not (empty? new-merge-requests) (play-file! :new-review-sound))
    (remember-merge-requests! merge-requests)))

(defn- to-milis [seconds] (* seconds 1000))
(defn -main
  [& args]
  (let [config-reload-iterval (to-milis (:config-reload-interval-sec @ctx/config))
        new-review-check-interval (to-milis (:new-review-check-interval-sec @ctx/config))]
    (info "Starting application...")
    (info "Rememberig current merge requests")
    (remember-merge-requests! (get-merge-requests))
    (info "Scheduling config reload every" config-reload-iterval " milis")
    (scheduler/every config-reload-iterval ctx/load-config ctx/scheduler-pool)
    (info "Scheduling new review checking task")
    (scheduler/every new-review-check-interval check-for-new-mrs! ctx/scheduler-pool)))
