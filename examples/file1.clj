(defn foo1 [x] (+ 100 x))

(defn foo2 [x & args] (reduce + (into [199 x] args)))

(defn foo3 [a b] (into {1 2 3 4 5 6} {a b}))
