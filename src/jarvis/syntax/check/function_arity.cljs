(ns jarvis.syntax.check.function-arity
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.scope :as scope]))

(defn- arity-error [fn-arity arg-num]
  (cond
    (= fn-arity :any) nil
    (nil? fn-arity) :not-a-function
    (< fn-arity arg-num) :too-many-args
    (> fn-arity arg-num) :not-enough-args))

(defn- check-arity [code]
  (let [type (-> code walk/info :type)
        value (-> code walk/value)
        is-call (-> code walk/info :is-call)
        scope (-> code walk/info :scope)]
    (if is-call
      (let [fn-arity (scope/fn-arity scope (-> value first walk/value))
            arg-num (- (count value) 2)]
        (arity-error fn-arity arg-num))
      nil)))

(defn annotate-error [code]
  (walk/with-err code (check-arity code)))

(defn check [code] (walk/postwalk annotate-error code))

