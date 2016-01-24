(ns jarvis.syntax.check.function-arity
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.scope :as scope]
            [jarvis.util.logger :as util]))

(defn- arity-error [fn-arity arg-num]
  (cond
    (= fn-arity :any) nil
    (= fn-arity arg-num) nil
    (nil? fn-arity) :not-a-function
    (< fn-arity arg-num) :too-many-args
    (> fn-arity arg-num) :not-enough-args))

(defn- check-arity [cb code]
  (let [info (walk/info code)
        type (:type info)
        id (:id info)
        is-call (:is-call info)
        scope (:scope info)
        value (-> code walk/value)]
    ;; (util/log! "Checking (fa)" id value info)
    (when is-call
      (let [fn-name (-> value first walk/value)
            fn-arity (scope/fn-arity scope fn-name)
            arg-num (- (count value) 1)
            err  (arity-error fn-arity arg-num)]
        (when-not (nil? err)
          (cb id err))))))

(defn check [cb code] (walk/each (partial check-arity cb) code))

