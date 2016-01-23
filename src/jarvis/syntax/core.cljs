(ns jarvis.syntax.core
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.types :as types]
            [jarvis.syntax.scope :as scope]
            [jarvis.syntax.check.core :as check]
            [jarvis.syntax.semantics :as function]))

(defn parse [code] (->> code walk/wrap types/parse function/parse scope/parse))

(defn check [code] (->> code check/check))
