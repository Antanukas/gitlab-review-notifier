(ns gitlab-review-notifier.tts
  (:require [clj-http.client :as client]
            [gitlab-review-notifier.context :as ctx]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [gitlab-review-notifier.player :as player]))
(taoensso.timbre/refer-timbre)

(defn- replace-map
  "given an input string and a hash-map, returns a new string with all
  keys in map found in input replaced with the value of the key"
  [s m]
  (clojure.string/replace
   s
   (re-pattern (apply str (interpose "|" (map #(java.util.regex.Pattern/quote %) (keys m)))))
   m))
(defn- replace-lithuanian [text]
  ;because google tts doesn't work very well in english mode with lithuanian names
  (replace-map text {"ą" "a" "č" "c" "ę" "e" "ė" "e" "į" "i" "š" "s" "ų" "u" "ū" "u" "ž" "z"}))

(defn- speech-to-file! [url params client-f]
  (let [tmp-dir (:tmp-dir @ctx/config)
        file-name (str (java.util.UUID/randomUUID) ".mp3")
        file-path (str tmp-dir "/" file-name)
        use-proxy? (:http-use-proxy @ctx/config)
        proxy-cfg (if use-proxy? {:proxy-host (:http-proxy-host @ctx/config) :proxy-port (:http-proxy-port @ctx/config)} {})
        request-params (merge proxy-cfg params {:as :stream})]
    (debug "Speech to file url " url " params: " request-params)
    (with-open [body-stream (:body (client-f url request-params))]
      (io/copy body-stream (io/file file-path)))
    file-path))

(defn- google-speech-to-file! [phrase]
  (let [language (:google-tts-language @ctx/config)
        ;Google limits to 100 chars only :/
        phrase-truncated (apply str (take 100 phrase))]
    (info "Speaking: " phrase-truncated)
    (speech-to-file! (:google-tts-url @ctx/config)
                     {:query-params {:tl language :q phrase-truncated}}
                     (partial client/get))))

(defn- acapela-speech-to-file! [phrase]
  (let [url (:acapela-tts-url @ctx/config)
        form-params {:form-params {:req_asw_type "STREAM" :cl_login "EVAL_VAAS"
                                   :cl_app (:acapela-tts-usr @ctx/config)
                                   :cl_pwd (:acapela-tts-psw @ctx/config)
                                   :req_voice "willlittlecreature22k"
                                   :req_text phrase :req_snd_type "MP3"}}]
    (speech-to-file! url form-params (partial client/post))))

(defn speak! [phrase]
  (let [phrase-normalized (replace-lithuanian (clojure.string/lower-case phrase))
        speech-file (acapela-speech-to-file! phrase-normalized)]
    (player/play-file! speech-file)
    (io/delete-file speech-file)))
