(ns gitlab-review-notifier.core
  (:gen-class)
  (:require [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.merge-requests :as mrs]
            [overtone.at-at :as scheduler]
            [taoensso.timbre :as timbre]))

;convinience aliases for timbre logging library
(timbre/refer-timbre)

(defn- to-milis [seconds] (* seconds 1000))
(defn- wrap-with-exception-logging [f]
  (fn [] (try (f) (catch Exception e (error e "Task execution failed.")))))

(defn -main
  [& args]
  (info "Starting application...")
  (info "Loading config")
  (ctx/load-config)
  (info "Rememberig current merge requests")
  (mrs/remember-merge-requests! (mrs/get-merge-requests))
  (let [config-reload-iterval (to-milis (:config-reload-interval-sec @ctx/config))
        new-review-check-interval (to-milis (:new-review-check-interval-sec @ctx/config))
        expired-review-check-interval (to-milis (:expired-review-check-interval-sec @ctx/config))]
    (info "Scheduling config reload every" config-reload-iterval "milis")
    (scheduler/every
     config-reload-iterval
     (wrap-with-exception-logging ctx/load-config)
     ctx/scheduler-pool)
    (info "Scheduling new review checking every" new-review-check-interval "milis")
    (scheduler/every
     new-review-check-interval
     (wrap-with-exception-logging mrs/check-for-new-mrs!)
     ctx/scheduler-pool)
    (info "Scheduling expired review checking every" expired-review-check-interval "milis")
    (scheduler/every
     expired-review-check-interval
     (wrap-with-exception-logging mrs/check-for-expired-mrs!)
     ctx/scheduler-pool)))

