





(ns team-speaker.rest.main-shim (:gen-class))

(defn -main [& args]
  (require 'team-speaker.rest.app)
  ((resolve 'team-speaker.rest.app/-main)))
