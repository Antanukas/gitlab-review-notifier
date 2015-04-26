(ns team-speaker.app
  (:require
    [compojure.core :refer :all]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [team-speaker.scheduler :as s]
    [cheshire.generate :as json]
    [team-speaker.merge-requests :as mrs]
    [team-speaker.context :as ctx]
    [team-speaker.routes :as routes]))
(taoensso.timbre/refer-timbre)

(defn joda-encoder [c jsonGenerator]
  (.writeString jsonGenerator (.toString c)))

(defn init-ctx []
  (info "Initiating context...")
  (info "Loading config")
  (ctx/load-config)
  (info "Rememberig current merge requests")
  (mrs/remember-merge-requests! (mrs/get-merge-requests))
  (s/init))

;Entry point of the application
(def app
  (do
    (json/add-encoder org.joda.time.DateTime joda-encoder)
    (init-ctx)
    (-> routes/app-routes
        (wrap-json-body)
        (wrap-json-response)
        (wrap-defaults api-defaults))))

