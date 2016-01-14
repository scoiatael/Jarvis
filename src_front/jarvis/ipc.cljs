(ns jarvis.ipc
  (:require [jarvis.util :as util]))

(def ^:private ipcRenderer (.-ipcRenderer (js/require "electron")))

(defn- send [ev msg] (.send ipcRenderer ev (clj->js msg)))

(defn- eval-message [form]
  {"cmd" "eval"
   "data" form})

(defn eval [form] (send "nrepl" (eval-message form)))

(defn open-file [fn] (send "open-file" fn))

(defn setup! []
  (.on ipcRenderer "done" (fn [data] (util/log! "IPC done!"))))
