(ns jarvis.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.file :as file]
            [jarvis.util :as util]
            [jarvis.nrepl :as nrepl]))

(def renderer (.-ipcRenderer (nodejs/require "electron")))

(defn start-server! [params] (.send renderer "start-server" (clj->js params)))
