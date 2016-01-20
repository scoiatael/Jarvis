(ns jarvis.syntax.types
  (:require [jarvis.syntax.walk :as walk]))

(defn simple-type [value] (let [vty (type value)]
                            (cond
                              (nil? value) :nil
                              (number? value) :number
                              (string? value) :string
                              (= cljs.core/PersistentVector vty) :vector
                              (= cljs.core/PersistentHashMap vty) :map
                              (= cljs.core/PersistentArrayMap vty) :map
                              (= cljs.core/List vty) :list
                              (= cljs.core/EmptyList vty) :list
                              (= cljs.core/Symbol vty) :symbol
                              (= cljs.core/Keyword vty) :keyword
                              :else vty)))

(defn annotate-type [code] {:value code :type (simple-type code)})

(defn parse [code] (walk/postwalk annotate-type code))
