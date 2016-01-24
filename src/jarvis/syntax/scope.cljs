(ns jarvis.syntax.scope
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]))

(def ^:private ^:const keyword-introducing-scope
  #{'defn
    'let
    'fn})

(defn introduces-scope? [value]
  (contains? keyword-introducing-scope (-> value first walk/value)))

(defprotocol Scope
  (fn-arity [this fn-name cb])
  (var-defined? [this var cb]))

(def ^:private ^:const global-vars
  (into keyword-introducing-scope #{'def
                                    '+
                                    '-
                                    '=}))

(defn- list-contains? [list elem]
  (some #(= % elem) list))

(defrecord EmptyScope []
  Scope
  (fn-arity [this fn-name cb] (var-defined? this fn-name #(cb (if % :any nil))))
  (var-defined? [this var cb] (cb (contains? global-vars var))))

(defn- bindings-let [this]
  (->> this :bindings (partition 2)))

(defn- list->fn-name [this]
  (let [name (first this)]
      (if (satisfies? walk/Info name)
        (walk/value name)
        nil)))

(defrecord LetScope [root bindings]
  Scope
  (fn-arity [this fn-name cb]
    (let [fn-def (->> this bindings-let (filter #(= fn-name (list->fn-name %))) first last)]
      (if (nil? fn-def)
        (fn-arity (:root this) fn-name cb)
        (-> fn-def walk/info :fn-arity cb))))
  (var-defined? [this var cb]
    (if (-> (->> this bindings-let (map list->fn-name)) (list-contains? var))
      (cb true)
      (var-defined? (:root this) var cb))))

(defrecord FnScope [root arguments]
  Scope
  (fn-arity [this fn-name cb]
    (if (list-contains? (:arguments this) fn-name)
      (cb :any)
      (fn-arity (:root this) fn-name cb)))
  (var-defined? [this var cb]
    (if (list-contains? (:arguments this) var)
      (cb true)
      (var-defined? (:root this) var cb))))

(defn- scope-of-let [root-scope value]
  (LetScope. root-scope (-> value (nth 1) walk/value)))

(defn- scope-of-defn [root-scope value]
  (let [arguments (-> value (nth 2) walk/value walk/strip)]
    (FnScope. root-scope arguments)))

(defn- scope-of-fn [root-scope value]
  (FnScope.  root-scope (-> value (nth 1) walk/value walk/strip)))

(defn- scope [root-scope code]
  (let [type (-> code walk/info :type)
        is-call (-> code walk/info :is-call)
        value (walk/value code)]
    (if (and is-call (introduces-scope? value))
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
