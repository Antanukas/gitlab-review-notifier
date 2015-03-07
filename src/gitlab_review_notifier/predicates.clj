(ns gitlab-review-notifier.predicates
  (:require [gitlab-review-notifier.context :as ctx]
            [clj-time.core :as t]))

(defn- is-mr-created-at-after-cofigure? [mr cfg-expire-key]
  (let [expiration-minutes (t/minutes (cfg-expire-key @ctx/config))
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
  (fn [mr] (not (contains? prev-mrs (:id mr)))))
