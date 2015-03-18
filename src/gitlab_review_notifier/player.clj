(ns gitlab-review-notifier.player
  (:require [clojure.java.io :as io]))

(defn play-file! [file-name]
  (with-open [fis (io/input-stream file-name)
              bis (java.io.BufferedInputStream. fis)
              player (javazoom.jl.player.Player. bis)]
    (.play player)))
