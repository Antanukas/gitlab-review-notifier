(ns team-speaker.client.jenkins-client
  (:require [clj-http.client :as client]))

(defn- get-jenkins [url path]
  (:body (client/get (str url path) {:as :json})))

(defn get-view-builds [url view-name]
  (get-jenkins url (str "view/" view-name "/api/json?pretty=true&tree=jobs[name,builds[result,number,culprits[fullName]]]")))
