(ns jarvis.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.file :as file]
            [jarvis.util :as util]
            [jarvis.nrepl :as nrepl]))

(defonce renderer (.-ipcRenderer (nodejs/require "electron")))

(defn handle-open-file [ev arg]
  (util/log! "Got open file" arg)
  (let [fname "/tmp/l/test.clj"]
    (file/open fname #(nrepl/open! fname))))

(defn start-server! [params] (.send renderer "start-server" (clj->js params)))
