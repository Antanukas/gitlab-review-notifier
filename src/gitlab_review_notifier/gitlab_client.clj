(ns gitlab-review-notifier.gitlab-client
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [gitlab-review-notifier.context :as ctx]
            [taoensso.timbre :as timbre]))
(timbre/refer-timbre)

(defn- gitlab-url []
  (:gitlab-url @ctx/config))
(defn- gitlab-token []
  (:gitlab-token @ctx/config))
(defn- url [& path-elements]
  (str (gitlab-url) "api/v3/" (apply str path-elements)))

(defn- do-get
  ([url] (do-get url {}))
  ([url query-params]
   (debug "Start do-get" url query-params)
   (let [response (:body
     (client/get url
                 {:accept :json
                  :query-params (merge query-params {:private_token (gitlab-token)})
                  :as :json}))]
     (debug "End do-get" response)
     response)))

(defn get-projects []
  (do-get (url "projects")))

(defn get-open-merge-requests [project-id]
  ;TODO find a better way to map json dates
  (let [mrs (do-get (url "projects/" project-id "/merge_requests") {:state "opened"})
        mrs-with-craeted-at-dates (map #(assoc %1 :created_at (f/parse (:created_at %1))) mrs)]
  mrs-with-craeted-at-dates))

;uncomment to use for local testing
(comment
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
)
