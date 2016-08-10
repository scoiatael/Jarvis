(ns jarvis.util.file
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.logger :as util]))

(def ^:private fs (nodejs/require "fs"))
(def ^:private path (nodejs/require "path"))
(def ^:private electron (.-remote (nodejs/require "electron")))
(def ^:private dialog (.-dialog electron))

(def ^:private initial-contents "")
(def ^:private open-file-options {:filters [{:name "Clojure" :extensions ["clj" "cljs" "cljc"]},
                                            {:name "All Files" :extensions ["*"]}]})

(defn open-file-dialog [cb]
  (.showOpenDialog dialog open-file-options cb))


(defn save-file-dialog [cb]
  (.showSaveDialog dialog open-file-options cb))

(defn dirname [fname]
  (.dirname path fname))

(defn read [file cb]
  (util/log! "ReadFile: " file)
  (.readFile fs file "utf8" (fn [err data]
                              (if (nil? err)
                                (cb data)
                                (util/error! err)))))

(defn write [file contents cb]
  (util/log! "WriteFile: " file)
  (.writeFile fs file contents (fn [err]
                                 (if (nil? err)
                                   (cb)
                                   (util/error! err)))))

(defn open [file cb]
  (util/log! "OpenFile: " file)
  (.access fs file (fn [err]
                     (if (nil? err)
                       (read file cb)
                       (write file initial-contents #(cb initial-contents))))))
