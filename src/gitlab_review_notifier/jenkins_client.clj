(ns gitlab-review-notifier.jenkins-client
  (:require [gitlab-review-notifier.context :as ctx]
            [clj-time.core :as t]
            [clj-http.client :as client]))

(defn- get-jenkins [path]
  (let [url (str (:jenkins-url @ctx/config) path)]
      (:body (client/get url {:as :json}))))

(defn get-view-builds [view-name]
  (get-jenkins (str "view/" view-name "/api/json?pretty=true&tree=jobs[name,builds[result,number,culprits[fullName]]]")))
