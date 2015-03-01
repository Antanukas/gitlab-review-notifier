(ns gitlab-review-notifier.gitlab-client
  (:require [clj-time.core :as t]))

(defn get-open-merge-requests [project-id]
  [{ :id "1" :title "Test MR 1" :created-at (t/minus (t/now) (t/seconds 120))}
   { :id "2" :title "Test MR 2" :created-at (t/minus (t/now) (t/seconds 60))}
   { :id "3" :title "Test MR 3" :created-at (t/minus (t/now) (t/seconds 30))}])

