(ns jarvis.util.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]))

(def renderer (.-ipcRenderer (nodejs/require "electron")))

(defn- params [dir] (clj->js {:projectPath dir}))

(defn start-server! []
  (util/log! "Requesting nREPL start..")
  (.send renderer "start-server" (params nil)))

(defn restart-server! [dir]
  (util/log! "Requesting nREPL restart..")
  (.send renderer "restart-server" (params dir)))
