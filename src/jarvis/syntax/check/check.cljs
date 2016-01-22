(ns jarvis.syntax.check.core
  (:require [jarvis.syntax.check.variable_defined :as var_defined]))

(defn check [code] (->> code var_defined/check))

