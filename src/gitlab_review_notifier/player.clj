(ns gitlab-review-notifier.player
  (:require [clojure.java.io :as io]))

(defn play-file!
  ([file-name] (play-file! file-name (Integer/MAX_VALUE)))
  ([file-name frames]
   (with-open [fis (io/input-stream file-name)
               bis (java.io.BufferedInputStream. fis)
               player (javazoom.jl.player.Player. bis)]
     ;TODO lets hope this interruptio will work someday
     (while (and (not (.isInterrupted (Thread/currentThread))) (.play player frames))))))
