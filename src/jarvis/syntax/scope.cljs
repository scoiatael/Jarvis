(ns jarvis.syntax.scope
  (:require [jarvis.syntax.walk :as walk]))

(def ^:private ^:const keyword-introducing-scope
  #{'defn
    'fn})

(defn introduces-scope? [type value]
  (and
   (= :list type)
   (not (empty? value))
   (contains? keyword-introducing-scope (-> value first walk/value))))

(defprotocol Scope
  (var-defined? [this var]))

(defrecord EmptyScope []
  Scope
  (var-defined? [this var] false))

(defrecord FunctionScope [root arguments]
  Scope
  (var-defined? [this var] (or (contains? (:arguments this) var)
                               (var-defined? (:root this) var))))

(defn scope [root-scope code]
  (let [type (-> code walk/info :type)
        value (walk/value code)]
    (if (introduces-scope? type value)
      (case (-> value first walk/value)
        defn (FunctionScope. root-scope (-> value (nth 2) walk/value walk/strip))
        fn (FunctionScope. root-scope (-> value (nth 1) walk/value walk/strip)))
      root-scope)))

(defn annotate-scope [root-scope code]
  (if (satisfies? walk/Info code)
    (let [scope (scope root-scope code)
          scoped-code (walk/with-info code {:scope scope})]
      (walk/walk #(annotate-scope scope %) identity scoped-code))
    (walk/walk #(annotate-scope root-scope %) identity code)))

(defn parse [code] (annotate-scope (EmptyScope.) code))
