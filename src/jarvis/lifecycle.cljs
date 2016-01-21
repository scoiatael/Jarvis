(ns jarvis.lifecycle
  (:require [jarvis.util.nrepl :as nrepl]
            [jarvis.util.file :as file]
            [jarvis.state.core :as state]
            [jarvis.syntax.parser :as parser]
            [jarvis.syntax.core :as t]
            [jarvis.util.logger :as util]))

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
         (state/push-code! (t/parse form) index))
       (state/set-error! error)))))

(defn push-file [contents]
  (state/reset-state!)
  (let [parsed (->> contents parser/file (map t/parse))]
    (map state/push-code! parsed)))

(defn add-new-node []
  (state/push-code! (t/parse '()))
  (let [last (- (state/nodes-length) 1)]
    (state/set-modal! [(state/code last) last])))

(defn pop-code [] (state/pop-code!))

(defn set-active [active] (state/set-active! active))
(defn set-modal [modal] (state/set-modal! modal))
(defn reset-error [] (state/reset-error!))
(defn reset-modal [] (state/reset-modal!))

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
