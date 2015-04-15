(ns team-speaker.working-days
  (:require [clj-time.core :as t]
            [clj-time.local :as l]))

;TODO working with local dates are pain.
;I guess I need new module for that
(defn- minus-day [[start end]]
  [(t/minus start (t/days 1)) (t/minus end (t/days 1))])

(defn- within-or-before [date [start end]]
  (or (t/within? start end date) (t/before? date start)))

(defn- minutes-till-interval-end [[start-date end-date] date]
  (cond
    (t/within? start-date end-date date) (t/in-minutes (t/interval date end-date))
    ;only count interval length when date is before start0date
    (t/before? date start-date) (t/in-minutes (t/interval start-date end-date))
    ;means after end-date. Means zero
    :else 0))

(defn- minute-count [date intervals]
  (let [oldest-interval (last intervals)
        without-oldest (drop-last intervals)
        minutes-till-day-end (minutes-till-interval-end oldest-interval date)]
    (->> without-oldest
         (map (partial apply t/interval))
         (map t/in-minutes)
         (reduce + minutes-till-day-end))))

(defn- copy-no-time [date hours minutes]
  (l/to-local-date-time
    (t/date-time
      (t/year date)
      (t/month date)
      (t/day date)
      hours
      minutes)))

(def working-day? #{1 2 3 4 5})
(defn is-working-moment? [moment-date [start-h start-m] [end-h end-m]]
  (and
    (working-day? (t/day-of-week moment-date))
    (t/within?
      (copy-no-time moment-date start-h start-m)
      (copy-no-time moment-date end-h end-m)
      moment-date)))

(defn working-minute-count [date [start-h start-min] [end-h end-min]]
  {:pre [(< (+ (* start-h 60) start-min)) (+ (* end-h 60) end-min)]}
  (->> [(t/today-at start-h start-min) (t/today-at end-h end-min)]
       (iterate minus-day)
       (take-while (partial within-or-before date))
       (filter #(-> (first %1) t/day-of-week working-day?))
       (minute-count date)))
