(ns gitlab-review-notifier.player)

(defn play-file [filename & opts]
  (let [fis (java.io.FileInputStream. filename)
        bis (java.io.BufferedInputStream. fis)
        player (javazoom.jl.player.Player. bis)]
    (if-let [synchronously (first opts)]
      (doto player
        (.play)
        (.close))
      (.start (Thread. #(doto player (.play) (.close)))))))
