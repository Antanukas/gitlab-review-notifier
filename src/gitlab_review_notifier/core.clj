(ns gitlab-review-notifier.core
  (:gen-class)
  (:require [gitlab-review-notifier.player :as player]
            [gitlab-review-notifier.config :as config]
            [gitlab-review-notifier.gitlab-client :as gitlab]
            [overtone.at-at :as scheduler]
            [clj-time.core :as t]))

(def scheduler-pool (scheduler/mk-pool))

;(scheduler/every 10000 #(player/play-file (:new-review-sound config/config) true) scheduler-pool)
(def prev-merge-request-ids (atom #{}))

(defn- is-mr-created-at-after-cofigure? [mr cfg-expire-key]
  (let [expiration-minutes (t/minutes (cfg-expire-key config/config))
        expiration-date (t/plus (:created-at mr) expiration-minutes)]
    (t/after? (t/now) expiration-date)))

;Predicates to check review request
(defn is-expired-mr? [mr]
  (is-mr-created-at-after-cofigure? mr :minutes-after-review-expires))
(defn is-close-to-expired-mr? [mr]
  "Tells if review is close to expiration but not yet expired"
  (and
   (not (is-expired-mr? mr))
   (is-mr-created-at-after-cofigure? mr :minutes-after-review-is-close-to-expire)))
(defn is-new-mr? [prev-mrs]
  (fn [mr] (true? (prev-mrs (:id mr)))))
;(is-expired-mr? {:created-at (t/minus (t/now) (t/minutes 29))})
;(is-close-to-expired-mr? {:created-at (t/minus (t/now) (t/minutes 29))})
((is-new-mr? #{"1"}) {:id "1"}) ;TODO pabaigti bl
(defn monitor-merge-requests []
  (let [merge-requests (gitlab/get-open-merge-requests)]
    (->> merge-requests
         ()
         (filter #(@prev-merge-request-ids (:id %1))))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (Thread/sleep 10000000))

;(player/play-file (:new-review-sound config/config) true)
(#{"a" "ab"} "ab")
