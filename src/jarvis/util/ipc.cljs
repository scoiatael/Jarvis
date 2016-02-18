(ns jarvis.util.ipc
  (:require [cljs.nodejs :as nodejs]))

(def renderer (.-ipcRenderer (nodejs/require "electron")))

(defn start-server! [params] (.send renderer "start-server" (clj->js params)))
