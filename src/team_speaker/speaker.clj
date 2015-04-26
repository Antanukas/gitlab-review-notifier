(ns team-speaker.speaker
  (:require
    [team-speaker.tts :as tts]
    [team-speaker.player :as player]
    [clojure.java.io :as io]))
(taoensso.timbre/refer-timbre)

;this flag is needed to track if multiple sources need to play music with background
(def is-play-another-iteration? (atom false))
;this flag tracks if currently some sound is being played.
;Allows to join other speakers to same background music
(def is-currently-paying? (atom false))
;won't play simulteniously
(def music-lock (Object.))
(def speak-lock (Object.))

;TODO why future  cacel doest work i java 8?
(defn- play-file-in-loop [file]
  (reset! is-play-another-iteration? true)
  (future
    (while (and (not @is-currently-paying?) @is-play-another-iteration?)
      (try
        (reset! is-currently-paying? true)
        (player/play-file! file)
        (finally (reset! is-currently-paying? false))))))

(defn- stop-play-file-in-loop [fut]
  (reset! is-play-another-iteration? false)
  (future-cancel fut))

(defn do-with-music [f]
  (locking music-lock
    (let [backgroud-music (play-file-in-loop (io/resource "sw-theme-volume.mp3"))]
      (try
        (f)
        ;TODO future cancel doesn't block so background music still plays when this exists
        (finally (stop-play-file-in-loop backgroud-music))))))

(defn speak! [phrase]
  (locking speak-lock
    (try
      (tts/speak! phrase)                                   ;google likes to reject my messages
      (catch Exception e (error e)))))
