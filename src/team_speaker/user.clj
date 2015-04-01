(ns team-speaker.user
  (:require [team-speaker.predicates :as p]
            [clj-time.core  :as t]))

(defn minus-day [[start end]]
  [(t/minus start (t/days 1)) (t/minus end (t/days 1))])

(defn within-or-before [date [start end]]
  (or (t/within? start end date) (t/before? date start)))

(defn minute-count [date intervals]
  (let [[first-start first-end] (last intervals)
        without-first (drop-last intervals)
        minutes-till-day-end (if (t/within? first-start first-end date)
                               (t/in-minutes (t/interval date first-end))
                               (t/in-minutes (t/interval first-start first-end)))]
    (println minutes-till-day-end)
    (->> without-first
         (map (partial apply t/interval))
         (map t/in-minutes)
         (reduce + minutes-till-day-end))))

(defn working-minute-count [date [start-h start-min] [end-h end-min]]
  {:pre [(< (+ (* start-h 60) start-min)) (+ (* end-h 60) end-min)]}
  (let [working-day? #{0 1 2 3 4 5}]
    (->> [(t/today-at start-h start-min) (t/today-at end-h end-min)]
         (iterate minus-day)
         (take-while (partial within-or-before date))
         (filter #(-> (first %1) t/day-of-week working-day?))
         (minute-count date))))