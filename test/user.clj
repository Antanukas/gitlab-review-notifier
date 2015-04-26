(ns ^{:skip-aot true} user
  (:require [team-speaker.calendar :as cal]))

(defn exec [f]
  (with-redefs [cal/is-working-moment? (fn [& args] true)]
    (f)))