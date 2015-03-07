(ns gitlab-review-notifier.predicates
  (:require [gitlab-review-notifier.context :as ctx]
            [clj-time.core :as t]))

(defn- is-mr-created-at-after-cofigure? [mr cfg-expire-key]
  (let [expiration-period-minutes (t/minutes (cfg-expire-key @ctx/config))
        expiration-date (t/plus (:created-at mr) expiration-period-minutes)]
    (t/after? (t/now) expiration-date)))

;Predicates to check review request
(defn is-expired-mr? [mr]
  (is-mr-created-at-after-cofigure? mr :review-expiration-min))

(defn is-new-mr? [prev-mrs]
  (fn [mr] (not (contains? prev-mrs (:id mr)))))
