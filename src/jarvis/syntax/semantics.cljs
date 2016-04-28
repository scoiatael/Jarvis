(ns jarvis.syntax.semantics
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.util.core :as util]))

(defn safe-count-arity [value pos]
  (let [arg-array (-> value (nth pos :not-found))]
    (if (= :not-found arg-array)
      nil
      (-> arg-array walk/value count))))

(defn- fn-arity [value]
  (case (-> value first walk/value)
    defn (safe-count-arity value 2)
    fn (safe-count-arity value 1)
    nil))

(defn- call? [code]
  (let [value (walk/value code)
        info (walk/info code)
        type (:type info)]
    (and
     (= :list type)
     (not (empty? value)))))

(defn annotate-semantics [code]
  (let [value (walk/value code)
        is-call (call? code)
        fn-arity (if is-call (fn-arity value) nil)]
    (-> (if (nil? fn-arity)
          code
          (walk/with-info code {:fn-arity fn-arity}))
        (walk/with-info {:is-call is-call}))))

(defn parse [code] (walk/postwalk annotate-semantics code))

