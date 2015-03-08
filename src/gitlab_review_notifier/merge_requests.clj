(ns gitlab-review-notifier.merge-requests
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [gitlab-review-notifier.predicates :as pred]
            [gitlab-review-notifier.context :as ctx]
            [taoensso.timbre :as timbre]))
(timbre/refer-timbre)

(def play-monitor (Object.))
;atom to check for new mrs based on previous
(def prev-merge-request-ids (atom #{}))

(defn get-merge-requests []
  (let [is-project-to-track? (fn [project] (:projects-to-track @ctx/config) (:name project))]
    (->> (gitlab/get-projects)
         (filter is-project-to-track?)
         (map :id)
         (map gitlab/get-open-merge-requests)
         (flatten))))

(defn- play-file! [file-path]
  (locking play-monitor ;lock so that multiple sounds won't be played simultaniously
    (player/play-file! file-path)))

(defn remember-merge-requests! [merge-requests]
  (reset! prev-merge-request-ids (set (map :id merge-requests))))

(defn check-for-new-mrs! []
  (debug "Entering check-for-new-mrs!")
  (let [merge-requests (get-merge-requests)
        new-merge-requests (filter (pred/is-new-mr? @prev-merge-request-ids) merge-requests)]
    (if-not (empty? new-merge-requests) (play-file! "bombpl.mp3"))
    (remember-merge-requests! merge-requests))
  (debug "Finished check-for-new-mrs!"))

(defn check-for-expired-mrs! []
  (debug "Entering check-for-expired-mrs!")
  (let [merge-requests (get-merge-requests)
        expired-mrs (filter pred/is-expired-mr? merge-requests)]
    (if-not (empty? expired-mrs)
      (do
        (info "Found" (count expired-mrs) "expired reviews!!!")
        (debug "Found reviews: " (clojure.string/join "," (map :title expired-mrs)))
        (play-file! "ALARM.mp3")))
    (debug "Finished check-for-expired-mrs!")))

