(ns jarvis.lifecycle
  (:require [jarvis.nrepl :as nrepl]
            [jarvis.file :as file]
            [jarvis.state :as state]
            [jarvis.util :as util]))

(def ^:private tmp-file "examples/file1.clj")

(defn open-file [fname]
  (util/log! "Got open file" fname)
  (let [fname tmp-file]
    (file/open fname (fn [contents]
                       (util/log! "File contents: " contents)
                       (nrepl/open! fname)
                       (state/push-file contents)))))

(defn write-file [fname contents]
  (util/log! "Got write file" fname contents)
  (let [fname tmp-file]
    (file/write fname contents #(nrepl/open! fname))))
