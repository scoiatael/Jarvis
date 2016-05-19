(ns jarvis.util.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]))

(def renderer (.-ipcRenderer (nodejs/require "electron")))

(defn start-server! [params]
  (util/log! "Requesting nREPL start..")
  (.send renderer "start-server" (clj->js params)))
