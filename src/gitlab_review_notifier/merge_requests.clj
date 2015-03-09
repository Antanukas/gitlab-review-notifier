(ns gitlab-review-notifier.merge-requests
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [gitlab-review-notifier.predicates :as pred]
            [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.tts :as tts]
            [taoensso.timbre :as timbre]))
(timbre/refer-timbre)

;atom to check for new mrs based on previous
(def prev-merge-request-ids (atom #{}))

(defn get-merge-requests []
  (let [is-project-to-track? (fn [project] (contains? (:projects-to-track @ctx/config) (:name project)))]
    (->> (gitlab/get-projects)
         (filter is-project-to-track?)
         (map :id)
         (map gitlab/get-open-merge-requests)
         (flatten))))

(defn- speak! [mr phrase-fn]
  (let [mr-title (:title mr)
        mr-author (get-in mr [:author :name])
        phrase (phrase-fn mr-title mr-author)]
    (try
      (tts/speak! phrase) ;google likes to reject my messages
      (catch Exception e (error e)))))

(defn- speak-new-request! [mr]
  (speak! mr (fn [mr-title mr-author] (str "New merge request " mr-title " was opened by " mr-author))))
(defn- speak-expired-request! [mr]
  (speak! mr (fn [mr-title mr-author] (str "Please review " mr-title " merge request opened by " mr-author " now!!!"))))


(defn remember-merge-requests! [merge-requests]
  (reset! prev-merge-request-ids (set (map :id merge-requests))))

(defn check-for-new-mrs! []
  (debug "Entering check-for-new-mrs!")
  (let [merge-requests (get-merge-requests)
        new-merge-requests (filter (pred/is-new-mr? @prev-merge-request-ids) merge-requests)]
    (remember-merge-requests! merge-requests)
    (doseq [mr new-merge-requests] (speak-new-request! mr)))
  (debug "Finished check-for-new-mrs!"))

(defn check-for-expired-mrs! []
  (debug "Entering check-for-expired-mrs!")
  (let [merge-requests (get-merge-requests)
        expired-mrs (filter pred/is-expired-mr? merge-requests)]
    (info "Found" (count expired-mrs) "expired reviews!!!")
    (debug "Found reviews: " (clojure.string/join "," (map :title expired-mrs)))
    (doseq [mr expired-mrs] (speak-expired-request! mr))
    (debug "Finished check-for-expired-mrs!")))

