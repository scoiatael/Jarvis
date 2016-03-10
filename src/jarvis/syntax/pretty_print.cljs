(ns jarvis.syntax.pretty-print
  (:require
   [cljs.pprint]
   [jarvis.syntax.core :as t]))

(defn- pretty-print [struct] (with-out-str (cljs.pprint/pprint struct)))

(defn pp [struct] (-> struct
                      t/strip
                      pretty-print))

