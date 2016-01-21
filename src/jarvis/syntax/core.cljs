(ns jarvis.syntax.core
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.syntax.types :as types]))

(defn parse [code] (->> code walk/wrap (walk/postwalk types/annotate-type)))
