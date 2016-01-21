(ns jarvis.syntax.core
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.types :as types]
            [jarvis.syntax.scope :as scope]))

(defn parse [code] (->> code walk/wrap types/parse scope/parse))
