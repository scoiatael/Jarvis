(ns jarvis.syntax.types
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]
            [jarvis.syntax.scope :refer [global-vars]]))

(defn symbol-type [symbol]
  (if (global-vars symbol)
    :reserved-symbol
    :symbol))

(defn simple-type [value]
  (let [vty (type value)]
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
      (= cljs.core/LazySeq vty) :list
      (= cljs.core/Symbol vty) (symbol-type value)
      (= cljs.core/Keyword vty) :keyword
      :else vty)))

(defn log-type [value]
  (let [type (simple-type value)]
    (util/log! value "->" type)
    type))

(defn annotate-type [code]
  {:pre [(satisfies? walk/Info code)]}
  (walk/with-info code {:type (-> code walk/value simple-type)}))

(defn parse [code] (walk/postwalk annotate-type code))

(defn sequelize [code]
  (walk/postwalk (fn [c]
                   (update-in c [:val]
                              #(if (seqable? %)
                                 (flatten (seq %))
                                 %)))
                 code))

(defn unsequelize [code]
  {:pre (walk/is-info? code)}
  (let [value (walk/value code)
        info (walk/info code)
        type (:type info)]
    (update-in code
               [:val] #(case type
                          :map (apply hash-map %)
                          :vector (into [] %)
                          %))))

(defn strip [form]
  (walk/postwalk #(-> % unsequelize walk/value) form))
