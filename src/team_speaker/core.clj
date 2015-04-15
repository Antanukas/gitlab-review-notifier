(ns team-speaker.core
  (:gen-class)
  (:require [team-speaker.context :as ctx]
            [team-speaker.merge-requests :as mrs]
            [team-speaker.builds :as builds]
            [team-speaker.working-days :as wd]
            [overtone.at-at :as scheduler]
            [clj-time.core :as t]
            [clj-time.local :as l]))
(taoensso.timbre/refer-timbre)

(defn- to-milis [seconds] (* seconds 1000))

(defn- wrap-with-exception-logging [f]
  (fn [] (try (f) (catch Exception e (error e "Task execution failed.")))))
(defn- wrap-with-working-time [f]
  (fn [] (if (wd/is-working-moment?
               (l/local-now)
               (:work-start @ctx/config)
               (:work-end @ctx/config))
           (f))))

(defn- same-or-after-day [date-time]
  (if (t/after? (l/local-now) date-time)
    date-time
    (t/plus date-time (t/days 1))))

(defn- today-at [hours minutes]
  (t/today-at hours minutes))

(defn- schedule-every [interval f]
  (scheduler/every
    interval
    (wrap-with-exception-logging
      (wrap-with-working-time f))
    ctx/scheduler-pool))

(defn- every-day-at
  ([[hours minutes] body] (every-day-at hours minutes body))
  ([hours minutes body]
    ;86400000 stands for one day
   (let [every-day-fun #(schedule-every  86400000 body)]
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
        failed-build-check-interval (to-milis (:failed-build-check-interval-sec @ctx/config))
        reminder-failed-build-interval (to-milis (:reminder-failed-build-interval-sec @ctx/config))
        [standup-hour standup-minute] (:standup-time @ctx/config)]
    (info "Scheduling config reload every" config-reload-iterval "milis")
    (schedule-every config-reload-iterval ctx/load-config)
    (info "Scheduling new review checking every" new-review-check-interval "milis")
    (schedule-every new-review-check-interval mrs/check-for-new-mrs!)
    (info "Scheduling expired review checking every" expired-review-check-interval "milis")
    (schedule-every expired-review-check-interval mrs/check-for-expired-mrs!)
    (info "Scheduling standup notifications")
    (every-day-at standup-hour standup-minute mrs/notify-about-standup-time!)
    (every-day-at standup-hour (- standup-minute 2) mrs/notify-about-near-standup!)
    (info "Scheduling failed build checkig every" failed-build-check-interval "milis")
    (schedule-every failed-build-check-interval builds/check-for-new-failed-builds!)
    (info "Scheduling reminder of failed builds every" reminder-failed-build-interval "milis")
    (schedule-every reminder-failed-build-interval builds/notify-about-failed-builds)))

