(ns team-speaker.jenkins-client
  (:require
    [team-speaker.context :as ctx]
    [clj-http.client :as client]))

(defn- get-jenkins [path]
  (let [url (str (:jenkins-url @ctx/config) path)]
      (:body (client/get url {:as :json}))))

(defn get-view-builds [view-name]
  (get-jenkins (str "view/" view-name "/api/json?pretty=true&tree=jobs[name,builds[result,number,culprits[fullName]]]")))
