(ns jarvis.ipc
  (:require [cljs.nodejs :as nodejs]
            [jarvis.file :as file]
            [jarvis.util :as util]
            [jarvis.nrepl :as nrepl]))

(defn- handle-nrepl-message [ev arg]
  (util/log! "Got nrepl message" arg)
  (let [cmd (.-cmd arg)
        data (.-data arg)
        id (.-id arg)]
    (case cmd
      "eval" (nrepl/eval! data)
      (util/error! "Unknown message" arg))))

(defn handle-open-file [ev arg]
  (util/log! "Got open file" arg)
  (let [fn "/tmp/l/test.clj"]
    (file/open fn #(nrepl/open! fn))))
