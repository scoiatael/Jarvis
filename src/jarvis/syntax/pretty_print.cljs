(ns jarvis.syntax.pretty-print
  (:require
   [cljs.pprint]
   [jarvis.syntax.walk :as walk]))

(defn- pretty-print [struct] (with-out-str (cljs.pprint/pprint struct)))

(defn pp [struct] (-> struct
                      walk/strip
                      pretty-print))

