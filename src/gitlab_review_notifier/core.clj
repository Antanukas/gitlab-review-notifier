(ns gitlab-review-notifier.core
  (:gen-class)
  (:require [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.merge-requests :as mrs]
            [overtone.at-at :as scheduler]
            [taoensso.timbre :as timbre]
            [clj-time.core :as t]
            [clj-time.local :as l]))

;convinience aliases for timbre logging library
(timbre/refer-timbre)

(defn- to-milis [seconds] (* seconds 1000))
(defn- wrap-with-exception-logging [f]
  (fn [] (try (f) (catch Exception e (error e "Task execution failed.")))))

(defn- same-or-after-day [date-time]
  (if (t/after? date-time (t/now))
    date-time
    (t/plus date-time (t/days 1))))

(defn- today-at [hours minutes]
  (t/today-at hours minutes))

(defn- every-day-at
  ([[hours minutes] body] (every-day-at hours minutes body))
  ([hours minutes body]
   ;86400000 stands for one day
   (let [every-day-fun #(scheduler/every 86400000 (wrap-with-exception-logging body) ctx/scheduler-pool)]
     ;lets kick off new schedule every day at specific time
     (scheduler/at (.getMillis (l/to-local-date-time (same-or-after-day (today-at hours minutes)))) every-day-fun ctx/scheduler-pool))))

(defn -main
  [& args]
  (info "Starting application...")
  (info "Loading config")
  (ctx/load-config)
  (info "Rememberig current merge requests")
  (mrs/remember-merge-requests! (mrs/get-merge-requests))
  (let [config-reload-iterval (to-milis (:config-reload-interval-sec @ctx/config))
        new-review-check-interval (to-milis (:new-review-check-interval-sec @ctx/config))
        expired-review-check-interval (to-milis (:expired-review-check-interval-sec @ctx/config))
        [standup-hour standup-minute] (:standup-time @ctx/config)]
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
     ctx/scheduler-pool)
    (info "Scheduling standup notifications")
    (every-day-at standup-hour standup-minute mrs/notify-about-standup-time!)
    (every-day-at standup-hour (- standup-minute 2) mrs/notify-about-near-standup!)))

