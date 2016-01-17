(ns jarvis.nrepl
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util :as util]))

(def ^:private Client (nodejs/require "nrepl-client"))

(def ^:private *connection* (atom nil))

(def ^:private port 31339)

(def ^:private connection-options {:port port :verbose true})

(defn- connect-to-server [cb]
  (reset! *connection* (.connect Client (clj->js connection-options)))
  (.once @*connection* "connect" cb))

(defn- handler [err res]
  (if-not (nil? err) (util/error! "nREPL error: " err))
  (util/log! "nREPL response: " res))

(defn- with-connection! [cb]
  (let [connection @*connection*]
    (if (nil? connection)
      (util/error! "No connection to nREPL!")
      (cb connection))))

(defn eval! [expr]
  (let [str-expr (if (string? expr)
                   expr
                   (str expr))]
    (with-connection! (fn [connection]
                        (util/log! "Eval: " str-expr)
                        (.eval connection str-expr handler)))))

(defn open! [file]
  (util/log! "Opening: " file)
  (eval! `(~'load-file ~file)))
