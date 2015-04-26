(ns team-speaker.main-shim (:gen-class))

(defn -main [& args]
  (require 'team-speaker.core)
  ((resolve 'team-speaker.core/init)))