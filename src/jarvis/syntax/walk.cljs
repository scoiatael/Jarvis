(ns jarvis.syntax.walk
  (:require [clojure.walk :as walk]))

(defprotocol Info
  (fresh [this value information])
  (info [this])
  (value [this])
  (with-info [this inf]))

(defrecord InfoImpl [val inf]
  Info
  (value [this] (:val this))
  (fresh [this value information] (InfoImpl. value information))
  (info [this] (:inf this))
  (with-info [this inf] (InfoImpl. (:val this) (into (or (:inf this) {})
                                                     inf))))
(defn walk
  [inner outer form]
  (cond
    (satisfies? Info form) (outer (fresh form (inner (value form)) (info form)))
    (list? form)   (outer (apply list (map inner form)))
    (seq? form)    (outer (doall (map inner form)))
    (record? form) (outer (reduce (fn [r x] (conj r (inner x))) form form))
    (map? form)    (outer (into (empty form) (map #(into [] (map inner %)) form)))
    (coll? form)   (outer (into (empty form) (map inner form)))
    :else          (outer form)))

(defn postwalk-all
  [f form]
  (walk (partial postwalk-all f) f form))

(defn wrap [form]
  (postwalk-all #(InfoImpl. % {}) form))

(defn postwalk [f form] (postwalk-all #(if (satisfies? Info %) (f %) %) form))

(defn strip [form]
  (postwalk value form))

(defn normalize-record [f]
  (if (record? f) (into {} f) f))

(defn normalize [form]
  (walk normalize identity (normalize-record form)))
