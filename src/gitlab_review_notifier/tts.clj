(ns gitlab-review-notifier.tts
  (:require [clj-http.client :as client]
            [gitlab-review-notifier.context :as ctx]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [gitlab-review-notifier.player :as player]
            [taoensso.timbre :as timbre]))
(timbre/refer-timbre)

(defn- replace-map
  "given an input string and a hash-map, returns a new string with all
  keys in map found in input replaced with the value of the key"
  [s m]
  (clojure.string/replace s
              (re-pattern (apply str (interpose "|" (map #(java.util.regex.Pattern/quote %) (keys m)))))
          m))
(defn- replace-lithuanian [text]
  ;because google tts doesn't work very well in english mode with lithuanian names
  (replace-map text {"ą" "a" "č" "c" "ę" "e" "ė" "e" "į" "i" "š" "s" "ų" "u" "ū" "u" "ž" "z"}))

(defn- google-speech-to-file! [phrase]
  (let [language (:tts-language @ctx/config)
        url (:tts-url @ctx/config)
        tmp-dir (:tmp-dir @ctx/config)
        file-name (str (java.util.UUID/randomUUID) ".mp3")
        file-path (str tmp-dir "/" file-name)
        use-proxy? (:http-use-proxy @ctx/config)
        proxy-cfg (if use-proxy? {:proxy-host (:http-proxy-host @ctx/config) :proxy-port (:http-proxy-port @ctx/config)} {})
        phrase-normalized (replace-lithuanian (clojure.string/lower-case phrase))
        phrase-limited (apply str (take 100 phrase-normalized)) ;Google limits to 100 chars only :/
        request-params (merge proxy-cfg {:as :stream :query-params {:tl language :q phrase-limited}})]
    (info "Speaking: " phrase-limited)
    (with-open [body-stream (:body (client/get url request-params))]
      (io/copy body-stream (io/file file-path)))
    file-path))

(defn speak! [phrase]
  (let [speech-file (google-speech-to-file! phrase)]
    (player/play-file! speech-file)
    (io/delete-file speech-file)))


