(ns gitlab-review-notifier.builds
  (:require [gitlab-review-notifier.jenkins-client :as jenkins]
            [gitlab-review-notifier.context :as ctx]
            [gitlab-review-notifier.predicates :as p]
            [gitlab-review-notifier.speaker :as s]))
(taoensso.timbre/refer-timbre)

(def failed-builds-atom (atom #{}))

(defn- speak-build-failed! [build]
  (let [job-name (clojure.string/replace (:name build)  #"-" " ")
        culprits (if (empty? (:culprits build)) #{"Nobody"} (:culprits build))
        culprits-text (clojure.string/join ", " culprits)]
  (s/speak! (str "Failed, build " job-name " has. " culprits-text " responsible are"))))

(defn- to-build-status [b]
  (let [latest-build (first (:builds b))
        culprit-names (set (map :fullName (:culprits latest-build)))]
    {:name (:name b) :status (:result latest-build) :culprits culprit-names}))

(defn- get-latest-build-statuses [view-name]
  (->> (jenkins/get-view-builds view-name)
       :jobs
       (map to-build-status)
       set))

(defn- union-minus-intersection [s1 s2]
  (let [union (clojure.set/union s1 s2)
        intersec (clojure.set/intersection s1 s2)]
     (clojure.set/difference union intersec)))

(defn- get-failed-builds [view-name]
  (let [latest-builds (get-latest-build-statuses view-name)]
    (set (filter (complement (p/is-build-with-status? "SUCCESS")) latest-builds))))

(defn notify-about-failed-builds
  ([] (notify-about-failed-builds @failed-builds-atom))
  ([failed-builds]
   (if-not (empty? failed-builds)
     (do
       (info "Still failing builds" (map :name failed-builds))
       (s/do-with-music #(doseq [b failed-builds] (speak-build-failed! b)))))))

(defn check-for-new-failed-builds! []
  (let [prev-failed-builds @failed-builds-atom
        latest-failed-builds (get-failed-builds (:jenkins-view-to-monitor @ctx/config))
        new-failed-builds (union-minus-intersection prev-failed-builds latest-failed-builds)]
    (reset! failed-builds-atom latest-failed-builds)
    (notify-about-failed-builds new-failed-builds)))
