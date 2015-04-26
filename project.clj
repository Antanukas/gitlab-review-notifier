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
                 [im.chit/cronj "1.4.1"]]
  :main ^:skip-aot team-speaker.main-shim
  :target-path "target/%s"
  :profiles {:uberjar {:aot [team-speaker.main-shim]}})
