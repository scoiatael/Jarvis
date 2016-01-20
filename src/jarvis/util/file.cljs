(ns jarvis.util.file
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.logger :as util]))

(def ^:private fs (nodejs/require "fs"))

(def ^:private initial-contents "")

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
