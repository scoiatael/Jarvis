(ns jarvis.types)

(defn simple-type [value] (let [vty (type value)]
                            (cond
                              (number? value) :number
                              (string? value) :string
                              (= cljs.core/PersistentVector vty) :vector
                              (= cljs.core/PersistentHashMap vty) :map
                              (= cljs.core/PersistentArrayMap vty) :map
                              (= cljs.core/List vty) :list
                              (= cljs.core/Symbol vty) :symbol
                              (= cljs.core/Keyword vty) :keyword
                              :else vty)))

(defn annotate-type [code] {:value code :type (simple-type code)})

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

(defn parse [code] (postwalk annotate-type code))
