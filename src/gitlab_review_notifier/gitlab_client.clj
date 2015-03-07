(ns gitlab-review-notifier.gitlab-client
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.java.io :as io]))

(require 'clojure.edn)
(defn read-data [file] (clojure.edn/read-string (slurp (io/resource file))))

(defn get-projects []
  (read-data "gitlab_test_projects.edn"))

;test responses by project id
(defn test-reviews []
  (read-data "gitlab_test_mrs.edn"))

(defn get-open-merge-requests [project-id]
  (map #(assoc %1 :created-at (f/parse (:created-at %1)))
       (get (test-reviews) project-id)))
