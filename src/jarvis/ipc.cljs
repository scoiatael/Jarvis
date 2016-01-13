(ns jarvis.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.nrepl :as nrepl]))

(def ^:private ipcMain (.-ipcMain (js/require "electron")))

(defn- handle-nrepl-message [ev arg]
  (.log js/console "Got message", arg)
  (let [cmd (.-cmd arg)
        data (.-data arg)
        id (.-id arg)]
    (case cmd
      "eval" (nrepl/eval! data)
      (.error js/console "Unknown message" arg))))

(defn- setup! []
  (.on ipcMain "nrepl" handle-nrepl-message))
