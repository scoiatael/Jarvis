(ns jarvis.ipc)

(def ^:private ipcRenderer (.-ipcRenderer (js/require "electron")))

(defn- eval-message [form]
  {"cmd" "eval"
   "id" 1
   "data" form})

(defn eval [form] (.send ipcRenderer "nrepl" (clj->js (eval-message form))))

(defn setup! [] "nil")
