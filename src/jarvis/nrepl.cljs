(ns jarvis.nrepl
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as async :refer [<!]]
            [jarvis.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private Client (nodejs/require "nrepl-client"))

(defonce ^:private *connection* (atom nil))

(def ^:private port 31339)

(def ^:private debug-connection false)

(def ^:private connection-options {:port port :verbose debug-connection})

(defn- connect-to-server [cb]
  (reset! *connection* (.connect Client (clj->js connection-options)))
  (.once @*connection* "connect" cb))

(defn- handler [ch err res]
  (if-not (nil? err) (util/error! "nREPL error: " err))
  (util/log! "nREPL response: " res)
  (if-let [val (.-value (aget res 0))]
    (go (>! ch res))
    (util/error! "No value received: " res)))

(defn- with-connection! [cb]
  (let [connection @*connection*]
    (if (nil? connection)
      (util/error! "No connection to nREPL!")
      (cb connection))))

(defn- eval [conn expr]
  (let [ch (async/chan)
        cb (partial handler ch)
        op (.eval conn expr cb)]
    ch))

(defn eval! [expr]
  (let [str-expr (if (string? expr)
                   expr
                   (str expr))]
    (with-connection! (fn [connection]
                        (util/log! "Eval: " str-expr)
                        (eval connection str-expr)))))

(defn open! [file]
  (util/log! "Opening: " file)
  (eval! `(~'load-file ~file)))

(defn ns! []
  (util/log! "Requesting current namespace")
  (eval! "*ns*"))
