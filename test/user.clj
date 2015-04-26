(ns ^{:skip-aot true} user
  (:require [team-speaker.core.calendar :as cal]))

(defn exec [f]
  (with-redefs [cal/is-working-moment? (fn [& args] true)]
    (f)))