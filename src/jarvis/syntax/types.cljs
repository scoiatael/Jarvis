(ns jarvis.syntax.types
  (:require [jarvis.syntax.walk :as walk]))

(defn simple-type [value] (let [vty (type value)]
                            (cond
                              (false? value) :bool
                              (true? value) :bool
                              (nil? value) :nil
                              (number? value) :number
                              (string? value) :string
                              (record? value) :record
                              (= cljs.core/PersistentVector vty) :vector
                              (= cljs.core/PersistentHashMap vty) :map
                              (= cljs.core/PersistentArrayMap vty) :map
                              (= cljs.core/List vty) :list
                              (= cljs.core/EmptyList vty) :list
                              (= cljs.core/Symbol vty) :symbol
                              (= cljs.core/Keyword vty) :keyword
                              :else vty)))

(defn annotate-type [code]
  ;; {:pre (satisfies? walk/Info code)}
  (walk/with-info code {:type (-> code walk/value simple-type)}))

(defn parse [code] (walk/postwalk annotate-type code))

(defn sequelize [code]
  (walk/postwalk (fn [c]
                   (update-in c [:val]
                              #(if (seqable? %)
                                 (seq %)
                                 %)))
                 code))
