(ns gitlab-review-notifier.gitlab-client
  (:require [clj-time.core :as t]))

(require 'clojure.edn)
(defn read-data [file] (clojure.edn/read-string (slurp file)))

(defn get-projects []
  (read-data "resources/gitlab_test_projects.edn"))

;test responses by project id
(defn test-reviews []
  (read-data "resources/gitlab_test_mrs.edn"))

(defn get-open-merge-requests [project-id]
  (get (test-reviews) project-id))
