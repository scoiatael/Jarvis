(ns jarvis.syntax.semantics
  (:require [jarvis.syntax.walk :as walk]))

(defn- fn-arity [value]
  (case (-> value first walk/value)
    defn (- (count (-> value (nth 2))) 1)
    fn (- (count (-> value (nth 1))) 1)
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

