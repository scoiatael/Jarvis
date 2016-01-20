(ns jarvis.syntax.walk)

(defn walk
  [inner outer form]
  (cond
    (list? form)   (outer (apply list (map inner form)))
    (seq? form)    (outer (doall (map inner form)))
    (record? form) (outer (reduce (fn [r x] (conj r (inner x))) form form))
    (map? form)    (outer (into (empty form) (map #(into [] (map inner %)) form)))
    (coll? form)   (outer (into (empty form) (map inner form)))
    :else          (outer form)))

(defn postwalk
  [f form]
  (walk (partial postwalk f) f form))

