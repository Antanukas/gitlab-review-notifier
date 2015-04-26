(ns team-speaker.core.context
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [taoensso.timbre :as timbre]))

(def config-file
  (or (io/file (System/getProperty "config.location")) (io/resource "config.edn")))

(def config (atom {}))

(defn load-config []
  (let [cfg (edn/read-string (slurp config-file))]
    (reset! config cfg)
    (timbre/set-level! (:log-level cfg))))




