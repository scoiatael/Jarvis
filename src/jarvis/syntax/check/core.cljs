(ns jarvis.syntax.check.core
  (:require [jarvis.syntax.check.variable-defined :as variable-defined]
            [jarvis.syntax.check.function-arity :as function-arity]
            [jarvis.util.logger :as util]))

(defn check [err-cb code]
  (variable-defined/check err-cb code)
  (function-arity/check err-cb code))

