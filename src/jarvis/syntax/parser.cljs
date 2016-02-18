(ns jarvis.syntax.parser
  (:require [cljs.tools.reader.edn :as edn]
            [cljs.tools.reader.reader-types :as types]
            [jarvis.util.logger :as util]))

(defn- reader [str]
  (types/source-logging-push-back-reader str))

(defn- file-part [acc rdr]
  (let [reader-options {}]
    ;; TODO make this a precondition
    ;; reader should be types/Reader
    ;; acc should be vector
    ;; reader is mutable!
    (let [rec #(file-part % rdr)]
      (try
        (rec (conj acc (edn/read {} rdr)))
        (catch ExceptionInfo e
          (if (= "EOF" (.-message e))
            acc
            (do
              (util/error! "Error while parsing file!" e)
              (rec acc))))))))

(defn file [contents]
  (file-part [] (reader contents)))

(defn form [contents]
  (try
    {:form (edn/read (reader contents))}
    (catch ExceptionInfo e
      (util/error! "Error while parsing form!" e)
      {:error e})))
