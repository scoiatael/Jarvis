(ns jarvis.bus
  (:require [cljs.nodejs :as nodejs]))

(def ^:private EventEmitter (nodejs/require "events"))

(defonce events (EventEmitter.))

