(ns jarvis.syntax.check.core
  (:require [jarvis.syntax.check.variable-defined :as variable-defined]
            [jarvis.syntax.check.function-arity :as function-arity]))

(defn check [code] (->> code variable-defined/check function-arity/check))

