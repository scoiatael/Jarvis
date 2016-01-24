(ns jarvis.lifecycle
  (:require [jarvis.util.nrepl :as nrepl]
            [jarvis.util.file :as file]
            [jarvis.state.core :as state]
            [jarvis.syntax.parser :as parser]
            [jarvis.syntax.core :as t]
            [jarvis.util.logger :as util]))

(defn- ingest-form [form]
  (->> form
       t/parse
       t/check
       ))

(defn push-code [code]
  (let [parsed (parser/form code)
        error (:error parsed)
        form (:form parsed)]
    (if (nil? error)
      (do
        (nrepl/eval! form)
        (state/push-code! (ingest-form form)))
      (state/set-error! error))))

(defn push-file [contents]
  (state/reset-state!)
  (let [parsed (->> contents parser/file (map ingest-form))]
    (map state/push-code! parsed)))

(defn set-modal [] (state/set-modal! true))
(defn add-new-node []
  (set-modal))

(defn pop-code [] (state/pop-code!))

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
