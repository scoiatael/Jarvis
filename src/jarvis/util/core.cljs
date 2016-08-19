(ns jarvis.util.core
  (:require [jarvis.util.render :as render]
            [jarvis.util.logger :as logger]))

(def dont-bubble render/dont-bubble)

(def error! logger/error!)
(def log! logger/log!)
(def logger logger/logger)

(defn- update-field [struct tuple]
  (let [field (first tuple)
        fun (last tuple)]
    (update-in struct field fun)))

(defn update-fields [state field fun & args]
  (reduce update-field state (conj (partition 2 args) [field fun])))
