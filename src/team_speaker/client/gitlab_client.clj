(ns team-speaker.client.gitlab-client
  (:require
    [clj-time.format :as f]
    [clj-http.client :as client]
    ;TODO circular dep
    [team-speaker.core.context :as ctx]))
(taoensso.timbre/refer-timbre)

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
        mrs-with-created-at-dates (map #(assoc %1 :created_at (f/parse (:created_at %1))) mrs)]
    mrs-with-created-at-dates))
