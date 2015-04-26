(ns team-speaker.client.gitlab-client
  (:require
    [clj-time.format :as f]
    [clj-http.client :as client]))
(taoensso.timbre/refer-timbre)

(defn- get-url [url & path-elements]
  (str url "api/v3/" (apply str path-elements)))

(defn- do-get
  ([url token] (do-get url token {}))
  ([url token query-params]
   (debug "Start do-get" url query-params)
   (let [response (:body
                   (client/get url
                               {:accept :json
                                :query-params (merge query-params {:private_token token})
                                :as :json}))]
     (debug "End do-get" response)
     response)))

(defn get-projects [url token]
  (do-get (get-url url "projects") token))

(defn get-open-merge-requests [url token project-id]
  ;TODO find a better way to map json dates
  (let [mrs (do-get (get-url url "projects/" project-id "/merge_requests") token {:state "opened"})
        mrs-with-created-at-dates (map #(assoc %1 :created_at (f/parse (:created_at %1))) mrs)]
    mrs-with-created-at-dates))
