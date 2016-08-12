(ns jarvis.syntax.scope
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.util.nrepl :as nrepl]
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

(def ^:const global-vars
  (into keyword-introducing-scope #{'def
                                    '+
                                    '-
                                    '=}))

(defn- list-contains? [list elem]
  (if (seqable? list)
    (some #(= % elem) list)
    nil))

(defrecord RootScope []
  Scope
  (fn-arity [this fn-name cb] (nrepl/fn-arity fn-name cb))
  (var-defined? [this var cb] (nrepl/var-defined? var cb)))

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
        name)))

(defrecord LetScope [root bindings]
  Scope
  (fn-arity [this fn-name cb]
    (let [fn-def (->> this bindings-let (filter #(= fn-name (list->fn-name %))) first last)]
      (if (nil? fn-def)
        (fn-arity (:root this) fn-name cb)
        (-> fn-def walk/info :fn-arity cb)
        )))
  (var-defined? [this var cb]
    (if (-> (->> this bindings-let (map list->fn-name)) (list-contains? var))
      (cb true)
      (do 
        ;; (util/error! var "not found in" this (->> this bindings-let (map list->fn-name)))
        (var-defined? (:root this) var cb)))))

(defrecord FnScope [root arguments]
  Scope
  (fn-arity [this fn-name cb]
    (if (list-contains? (-> this :arguments walk/strip) fn-name)
      (cb :any)
      (do
        ;; (util/error! fn-name "not found in" this)
        (fn-arity (:root this) fn-name cb))))
  (var-defined? [this var cb]
    (if (list-contains? (-> this :arguments walk/strip) var)
      (cb true)
      (do
        ;; (util/error! var "not found in" this)
        (var-defined? (:root this) var cb)))))

(defn- arguments-or-error [value pos]
  (let [arg-array (nth value pos :not-found)]
    (if (= arg-array :not-found)
      (util/error! "Error: not found" {:value value :pos pos})
      (if-let [args (-> arg-array walk/value)]
        {:arguments args}
        (util/error! "Error: not-an-array" {:value value :pos pos :arg-array arg-array})))))

(defn- scope-of-let [root-scope value]
  (if-let [arguments (:arguments (arguments-or-error value 1))]
    (LetScope. root-scope arguments)))

(defn- scope-of-defn [root-scope value]
  (if-let [arguments (:arguments (arguments-or-error value 2))]
    (FnScope. root-scope arguments)))

(defn- scope-of-fn [root-scope value]
  (if-let [arguments (:arguments (arguments-or-error value 1))]
    (FnScope. root-scope arguments)))

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
    (let [maybe-scope (scope root-scope code)
          scope (if (nil? maybe-scope) root-scope maybe-scope)
          scoped-code (walk/with-info code {:scope scope})]
      (walk/walk #(annotate-scope scope %) identity scoped-code))
    (walk/walk #(annotate-scope root-scope %) identity code)))

(defn parse [code] (annotate-scope (RootScope.) code))
