(ns jarvis.syntax.check.variable_defined
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.scope :as scope]))

(defn- check-scope [code]
  (let [type (-> code walk/info :type)
        value (-> code walk/value)
        scope (-> code walk/info :scope)]
    (if (= type :symbol)
      (if (scope/var-defined? scope value)
        nil
        :variable-not-defined)
      nil)))

(defn annotate-error [code]
  (walk/with-err code (check-scope code)))

(defn check [code] (walk/postwalk annotate-error code))
