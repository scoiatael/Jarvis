(ns jarvis.util.logger
  (:require [cljs.nodejs :as nodejs]))

(def ^:private ^:const winston (nodejs/require "winston"))
(def Console (-> winston .-transports .-Console))
(def File (-> winston .-transports .-File))

(def logpath (.join (nodejs/require "path")
                   (.tmpdir (nodejs/require "os"))
                   "Jarvis.log"))

(doto winston
  (.add File (clj->js {:filename logpath}))
  (.remove Console))

(when js/goog.DEBUG
  (.add winston Console))

(defn log [level msg meta] (.log winston level msg (clj->js meta)))

(defn error! [msg meta] (log "error" msg meta))

(defn log! [msg meta] (log "info" msg meta))
