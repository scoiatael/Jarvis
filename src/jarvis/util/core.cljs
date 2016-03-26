(ns jarvis.util.core
  (:require [jarvis.util.render :as render]
            [jarvis.util.logger :as logger]))

(def dont-bubble render/dont-bubble)

(def error! logger/error!)
(def log! logger/log!)
