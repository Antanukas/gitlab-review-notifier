(ns gitlab-review-notifier.merge-requests
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [gitlab-review-notifier.predicates :as pred]
            [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.tts :as tts]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]))
(timbre/refer-timbre)

;atom to check for new mrs based on previous
(def prev-merge-request-ids (atom #{}))
(def is-playing-file-in-loop? (atom false))


;TODO why future  cacel doest work i java 8?
(defn- play-file-in-loop [file]
  (reset! is-playing-file-in-loop? true)
  (future (while @is-playing-file-in-loop? (player/play-file! file))))

(defn- stop-play-file-in-loop [fut]
  (reset! is-playing-file-in-loop? false)
  (future-cancel fut))


(defn- speak! [phrase]
  (try
    (tts/speak! phrase) ;google likes to reject my messages
    (catch Exception e (error e))))

(defn- speak-new-request! [mr]
  (speak! (str "Reviewed, new merge request created by " (get-in mr [:author :name]) " must be.  Hmmmmmm.")))
(defn- speak-expired-request! [mr]
  (speak! (str "Review merge request created by " (get-in mr [:author :name]) ", somebody must.")))
(defn- speak-standup-time! [] (speak! "Time for daily standup, is it.  Hmmmmmm."))
(defn- speak-near-standup-time! [] (speak! "Near, daily standup time is.  Yeesssssss."))

(def music-lock (Object.)) ;won't play simulteniously
(defn- do-with-music [f]
  (locking music-lock
    (let [backgroud-music (play-file-in-loop (io/resource "sw-theme-volume.mp3"))]
      (try
        (f)
        ;TODO future cancel doesn't block so background music still plays when this exists
        (finally (stop-play-file-in-loop backgroud-music))))))

(defn get-merge-requests []
  (let [is-project-to-track? (fn [project] (contains? (:projects-to-track @ctx/config) (:name project)))]
    (->> (gitlab/get-projects)
         (filter is-project-to-track?)
         (map :id)
         (map gitlab/get-open-merge-requests)
         (flatten))))

(defn remember-merge-requests! [merge-requests]
  (reset! prev-merge-request-ids (set (map :id merge-requests))))


(defn check-for-new-mrs! []
  (debug "Entering check-for-new-mrs!")
  (let [merge-requests (get-merge-requests)
        new-merge-requests (filter (pred/is-new-mr? @prev-merge-request-ids) merge-requests)]
    (remember-merge-requests! merge-requests)
    (if-not (empty? new-merge-requests) (do-with-music #(doseq [mr new-merge-requests] (speak-new-request! mr))))
    (debug "Finished check-for-new-mrs!")))

(defn check-for-expired-mrs! []
  (debug "Entering check-for-expired-mrs!")
  (let [merge-requests (get-merge-requests)
        expired-mrs (filter pred/is-expired-mr? merge-requests)]
    (info "Found" (count expired-mrs) "expired reviews!!!")
    (debug "Found reviews: " (clojure.string/join "," (map :title expired-mrs)))
    (if-not (empty? expired-mrs) (do-with-music #(doseq [mr expired-mrs] (speak-expired-request! mr))))
    (debug "Finished check-for-expired-mrs!")))

(defn notify-about-standup-time! []
  (info "Notifying about standup")
  (speak-standup-time!)
  (info "Standup notification finished"))

(defn notify-about-near-standup! []
  (info "Notifying about near standup")
  (speak-near-standup-time!)
  (info "Notification about near standup finished"))
