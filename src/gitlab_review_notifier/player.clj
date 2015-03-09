(ns gitlab-review-notifier.player
  (:require [clojure.java.io :as io]))

;lock so that multiple sounds won't be played simultaniously
(def play-monitor (Object.))

(defn play-file! [file-name]
  (with-open [fis (io/input-stream file-name)
              bis (java.io.BufferedInputStream. fis)
              player (javazoom.jl.player.Player. bis)]
    (locking play-monitor (.play player))))
