(ns team-speaker.core.merge-requests
  (:require
    [team-speaker.client.gitlab-client :as gitlab]
    [team-speaker.core.predicates :as pred]
    [team-speaker.core.context :as ctx]
    [team-speaker.core.speaker :as s]))
(taoensso.timbre/refer-timbre)
;atom to check for new mrs based on previous
(def prev-merge-request-ids (atom #{}))

(defn- speak-new-request! [mr]
  (s/speak! (str "Reviewed, new merge request created by " (get-in mr [:author :name]) " must be.  Hmmmmmm.")))
(defn- speak-expired-request! [mr]
  (s/speak! (str "Review merge request created by " (get-in mr [:author :name]) ", somebody must.")))
(defn- speak-standup-time! [] (s/speak! "Time for daily standup, is it.  Hmmmmmm."))
(defn- speak-near-standup-time! [] (s/speak! "Near, daily standup time is.  Yeesssssss."))

(defn get-merge-requests []
  (let [is-project-to-track? (fn [project] (contains? (:projects-to-track @ctx/config) (:name project)))]
    (->> (gitlab/get-projects (:gitlab-url @ctx/config) (:gitlab-token @ctx/config))
         (filter is-project-to-track?)
         (map :id)
         (map (partial gitlab/get-open-merge-requests (:gitlab-url @ctx/config) (:gitlab-token @ctx/config)))
         (flatten))))

(defn remember-merge-requests! [merge-requests]
  (reset! prev-merge-request-ids (set (map :id merge-requests))))


(defn check-for-new-mrs! []
  (debug "Entering check-for-new-mrs!")
  (let [merge-requests (get-merge-requests)
        new-merge-requests (filter (pred/is-new-mr? @prev-merge-request-ids) merge-requests)]
    (remember-merge-requests! merge-requests)
    (if-not (empty? new-merge-requests) (s/do-with-music #(doseq [mr new-merge-requests] (speak-new-request! mr))))
    (debug "Finished check-for-new-mrs!")))

(defn check-for-expired-mrs! []
  (debug "Entering check-for-expired-mrs!")
  (let [merge-requests (get-merge-requests)
        expired-mrs (filter pred/is-expired-mr? merge-requests)]
    (info "Found" (count expired-mrs) "expired reviews!!!")
    (debug "Found reviews: " (clojure.string/join "," (map :title expired-mrs)))
    (if-not (empty? expired-mrs) (s/do-with-music #(doseq [mr expired-mrs] (speak-expired-request! mr))))
    (debug "Finished check-for-expired-mrs!")))

(defn notify-about-standup-time! []
  (info "Notifying about standup")
  (speak-standup-time!)
  (info "Standup notification finished"))

(defn notify-about-near-standup! []
  (info "Notifying about near standup")
  (speak-near-standup-time!)
  (info "Notification about near standup finished"))
