(ns jarvis.syntax.check.variable_defined
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.scope :as scope]
            [jarvis.util.logger :as util]))

(defn- check-scope [cb code]
  (let [info (walk/info code)
        type (:type info)
        scope (:scope info)
        id (:id info)
        value (walk/value code)]
    ;; (util/log! "Checking (vd)" id value info)
    (when (and (= type :symbol) (not (scope/var-defined? scope value)))
      (cb id
          :variable-not-defined))))

(defn check [ch code] (walk/each (partial check-scope ch) code))
