(ns jarvis.syntax.scope
  (:require [jarvis.syntax.walk :as walk]))

(def ^:private ^:const keyword-introducing-scope
  #{'defn
    'let
    'fn})

(defn introduces-scope? [type value]
  (and
   (= :list type)
   (not (empty? value))
   (contains? keyword-introducing-scope (-> value first walk/value))))

(defprotocol Scope
  (var-defined? [this var]))

(def ^:private ^:const global-vars
  (into keyword-introducing-scope #{'def
                                    '+
                                    '-
                                    '=}))

(defn- list-contains? [list elem]
  (some #(= % elem) list))

(defrecord EmptyScope []
  Scope
  (var-defined? [this var] (contains? global-vars var)))

(defrecord LetScope [root bindings]
  Scope
  (var-defined? [this var] (or (list-contains? (->> this :bindings (partition 2) (map first)) var)
                               (var-defined? (:root this) var))))

(defrecord FnScope [root arguments]
  Scope
  (var-defined? [this var] (or (list-contains? (:arguments this) var)
                               (var-defined? (:root this) var))))

(defn- scope-of-let [root-scope value]
  (LetScope. root-scope (-> value (nth 1) walk/value walk/strip)))

(defn- scope-of-defn [root-scope value]
  (let [arguments (-> value (nth 2) walk/value walk/strip)]
    (FnScope. root-scope arguments)))

(defn- scope-of-fn [root-scope value]
  (FnScope.  root-scope (-> value (nth 1) walk/value walk/strip)))

(defn- scope [root-scope code]
  (let [type (-> code walk/info :type)
        value (walk/value code)]
    (if (introduces-scope? type value)
      (case (-> value first walk/value)
        let (scope-of-let root-scope value)
        defn (scope-of-defn root-scope value)
        fn (scope-of-fn root-scope value))
      root-scope)))

(defn- annotate-scope [root-scope code]
  (if (satisfies? walk/Info code)
    (let [scope (scope root-scope code)
          scoped-code (walk/with-info code {:scope scope})]
      (walk/walk #(annotate-scope scope %) identity scoped-code))
    (walk/walk #(annotate-scope root-scope %) identity code)))

(defn parse [code] (annotate-scope (EmptyScope.) code))
