(defproject team-speaker "0.1.0-SNAPSHOT"
  :description "Team speaker is an app for producing sounds based on events from Gitlab or Jenkins."
  :url "https://github.com/Antanukas/team-speaker"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [javazoom/jlayer "1.0.1"]
                 [clj-time "0.9.0"]
                 [com.taoensso/timbre "3.4.0"]
                 [clj-http "1.0.1"]
                 [im.chit/cronj "1.4.1"]
                 [compojure "1.3.3"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]]
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.3"]]
  :ring {:handler team-speaker.rest.app/app}
  :main team-speaker.rest.main-shim
  :profiles {
    :uberjar {:aot [team-speaker.rest.main-shim]}
  })
