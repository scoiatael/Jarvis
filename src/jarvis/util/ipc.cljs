(ns jarvis.util.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]))

(def renderer (.-ipcRenderer (nodejs/require "electron")))

(def ^:dynamic ^:private params {})

(defn start-server! []
  (util/log! "Requesting nREPL start..")
  (.send renderer "start-server" (clj->js params)))

(defn restart-server! []
  (util/log! "Requesting nREPL restart..")
  (.send renderer "restart-server" (clj->js params)))
