(ns jarvis.nrepl
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util :as util]))

(def ^:private Client (nodejs/require "nrepl-client"))

(def ^:private *connection* (atom nil))

(def ^:private port 31339)

(defn- connect-to-server [cb]
  (reset! *connection* (.connect Client (clj->js {:port port})))
  (.once @*connection* "connect" cb))

(defn- handler [err res]
  (if-not (nil? err) (util/error! "nREPL error: " err))
  (util/log! "nREPL response: " res))

(defn eval! [expr] (let [connection @*connection*]
                        (util/log! "Eval: " expr)
                        (if (nil? connection)
                          (util/error! "No connection to nREPL!")
                          (.eval connection expr handler))))

(defn open! [file]
  (util/log! "Opening: " file)
  (eval! `(~'load-file ~file)))
