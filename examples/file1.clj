(defn foo1 [x] (+ 100 x))

(defn foo2 [x & args] (reduce + (into [199 x] args)))
