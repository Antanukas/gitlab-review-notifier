(ns team-speaker.routes
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.util.response :refer [response]]
    [team-speaker.scheduler :as s]))

(defroutes app-routes
           (GET "/" [] (response {:greeting "Hello world"}))
           (GET "/jobs" [] (response (s/get-current-schedule)))
           (route/not-found "Not Found"))
