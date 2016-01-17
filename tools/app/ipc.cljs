(ns app.ipc
  (:require [cljs.nodejs :as nodejs]))

(def main (.-ipcMain (nodejs/require "electron")))

(defn reply! [ev name reply]
  (-> ev .-sender (.send name (clj->js reply))))

