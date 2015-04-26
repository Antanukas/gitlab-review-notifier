(ns team-speaker.core.scheduler
  (:require
    [team-speaker.core.calendar :as cal]
    [team-speaker.core.context :as ctx]
    [team-speaker.core.merge-requests :as mrs]
    [team-speaker.core.builds :as builds]
    [clj-time.local :as l]
    [cronj.core :as sched]))
(taoensso.timbre/refer-timbre)

(def ^:private scheduler (atom {}))

(defn- wrap-with-exception-logging [f]
  (fn [t opts] (try (f t opts) (catch Exception e (error e "Task execution failed.")))))
(defn- wrap-with-working-time [f]
  (fn [t opts] (if (cal/is-working-moment?
                     (l/local-now)
                     (:work-start @ctx/config)
                     (:work-end @ctx/config))
                 (f t opts))))
(defn- wrap-no-args [f]
  (fn [t opts] (f)))

(defn create-task [id cron handler]
  {:id id
   :handler (wrap-with-exception-logging
              (wrap-with-working-time
                (wrap-no-args handler)))
   :schedule cron
   :opts {}})

(defn get-scheduler [] @scheduler)

(defn- to-current-schedule [conj-schedule]
  (let [task (:task conj-schedule)]
    (hash-map
      :schedule (:schedule conj-schedule)
      :id (:id task)
      :last-exec @(:last-exec task))))

(defn get-current-schedule []
  (map to-current-schedule (:scheduler @scheduler)))

(defn init []
  (let [config-reload-cron (:config-reload-cron @ctx/config)
        new-review-check-cron (:new-review-check-cron @ctx/config)
        expired-review-check-cron (:expired-review-check-cron @ctx/config)
        failed-build-check-cron (:failed-build-check-cron @ctx/config)
        reminder-failed-build-cron (:reminder-failed-build-cron @ctx/config)
        near-standup-cron (:near-standup-cron @ctx/config)
        standup-cron (:standup-cron @ctx/config)
        schedule [(create-task "config-reload" config-reload-cron ctx/load-config)
                  (create-task "new-review-check" new-review-check-cron mrs/check-for-new-mrs!)
                  (create-task "expired-review-check" expired-review-check-cron mrs/check-for-expired-mrs!)
                  (create-task "standup-time" standup-cron mrs/notify-about-standup-time!)
                  (create-task "near-standup" near-standup-cron mrs/notify-about-near-standup!)
                  (create-task "failed-builds" failed-build-check-cron builds/check-for-new-failed-builds!)
                  (create-task "reminder-failed-builds" reminder-failed-build-cron builds/notify-about-failed-builds)]
        cj (sched/cronj :entries schedule)]
       (do
         (reset! scheduler cj)
         (sched/start! cj))))

