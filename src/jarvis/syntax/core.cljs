(ns jarvis.syntax.core
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.types :as types]
            [jarvis.syntax.scope :as scope]
            [jarvis.syntax.check.core :as check]
            [jarvis.syntax.semantics :as function]))

(defn parse [code] (->> code walk/wrap types/parse types/sequelize function/parse scope/parse))

(defn check [ch code] (check/check ch code))

(defn strip [f] (types/strip f))
