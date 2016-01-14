(ns jarvis.pretty-print
  (:require
   [cljs.pprint]))

(defn pretty-print [struct] (with-out-str (cljs.pprint/pprint struct)))
