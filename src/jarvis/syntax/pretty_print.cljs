(ns jarvis.syntax.pretty-print
  (:require
   [cljs.pprint]
   [jarvis.syntax.walk :as walk]))

(defn- pretty-print [struct] (with-out-str (cljs.pprint/pprint struct)))

(defn- strip-annotations [struct]
  (if (and (map? struct) (contains? struct :value))
    (:value struct)
    struct))

(defn- strip-all [struct]
  (walk/postwalk strip-annotations struct))

(defn pp [struct] (-> struct
                      (strip-all)
                      (pretty-print)))

