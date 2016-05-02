(ns jarvis.events
  (:require [cljs.core.async :as async :refer [>! <!]]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def ^:private ^:constant *event-bus* (atom nil))
(def ^:private ^:constant *event-publisher* (atom nil))

(defn- on [ev f]
  (let [ch (async/chan)]
    (async/sub @*event-publisher* ev ch)
    (go-loop []
      (f (<! ch))
      (recur))))

(defn- publish! [ev]
  (go (>! @*event-publisher* ev)))

(defn init! []
  (reset! *event-bus* (async/chan))
  (reset! *event-publisher* (async/pub @*event-bus* #(:topic %)))
  )

