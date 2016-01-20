(ns jarvis.lifecycle
  (:require [jarvis.nrepl :as nrepl]
            [jarvis.file :as file]
            [jarvis.state :as state]
            [jarvis.syntax.parser :as parser]
            [jarvis.syntax.types :as t]
            [jarvis.util :as util]))

(defn push-code
  ([code]
   (push-code code nil))
  ([code index]
   (let [parsed (parser/form code)
         error (:error parsed)
         form (:form parsed)]
     (if (nil? error)
       (do
         (nrepl/eval! form)
         (state/push-code (t/parse form) index))
       (state/set-error error)))))

(defn push-file [contents]
  (state/reset-state!)
  (let [parsed (parser/file contents)]
    (map state/push-code parsed)))

(def ^:private tmp-file "examples/file1.clj")

(defn open-file [fname]
  (util/log! "Got open file" fname)
  (let [fname tmp-file]
    (file/open fname (fn [contents]
                       (util/log! "File contents: " contents)
                       (nrepl/open! fname)
                       (push-file contents)))))

(defn write-file [fname contents]
  (util/log! "Got write file" fname contents)
  (let [fname tmp-file]
    (file/write fname contents #(nrepl/open! fname))))

(defn add-new-node []
  (state/push-code (t/parse '()))
  (let [last (- (state/nodes-length!) 1)]
    (state/set-modal [(state/code! last) last])))

(defn pop-code [] (state/pop-code))
