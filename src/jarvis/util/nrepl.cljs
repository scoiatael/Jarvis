(ns jarvis.util.nrepl
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [cljs.core.async :as async :refer [<! >!]]
            [jarvis.util.logger :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private Client (nodejs/require "nrepl-client"))

(defonce ^:private *connection* (atom nil))

(def ^:private debug-connection false)

(def ^:private connection-options {:host "127.0.0.1"
                                   :verbose debug-connection})

(defn- connect-to-server [s cb]
  (let [port (:port s)
        options (assoc connection-options :port port)]
    (util/log! "Connecting to nrepl" options)
    (reset! *connection* (.connect Client (clj->js options)))
    (.once @*connection* "connect" cb)))

(defn extract-values [res]
  (let [val (.-value (aget res 0))
        ex (.-ex (aget res 0))
        error_pos (aget res 1)
        err (if (nil? error_pos) nil (.-err error_pos))
        status (.-status (last res))
        done (if (nil? status) false (= (first status) "done"))
        out (map #(.-out %) res)]
    {:val val
     :ex ex
     :err err
     :done? done
     :out out}))

(defn- handler [ch err res]
  (if-not (nil? err) (util/error! "nREPL error: " err))
  ;; (util/log! "nREPL response: " res)
  (let [val (extract-values res)]
    (when (:done? val)
      ;; (util/log! "Done: " val)
      (go (>! ch val)))))

(defn- with-connection! [cb]
  (let [connection @*connection*]
    (if (nil? connection)
      (util/error! "No connection to nREPL!" {})
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
                        ;; (util/log! "Eval: " str-expr)
                        (eval connection str-expr)))))

(defn open! [file]
  (util/log! "Opening: " file)
  (eval! `(~'load-file ~file)))

(defn ns! []
  (util/log! "Requesting current namespace" {})
  (eval! "*ns*"))

(defn doc! [var]
  (let [expr `(~'clojure.repl/doc ~(symbol var))]
    (eval! expr)))

(defn arglists! [var]
  (let [expr `(~':arglists (~'meta #'~(-> var str symbol)))]
    (eval! expr)))

(defn resolve! [var]
  (let [expr `(~'resolve '~(symbol var))]
    (eval! expr)))

(defn- no-err? [val]
  (-> val :ex nil?))

(defn- no-doc-err? [val]
  (-> val :val (= "nil") not))

(defn- parse-doc [val]
  (if (no-err? val) :any nil))

(defn fn-arity [fn-name cb]
    (go (-> fn-name arglists! <! parse-doc cb)))

(defn var-defined? [var cb]
  (go (-> var resolve! <! no-doc-err? cb)))

(defn parse-functions [funs]
  (if (string? funs)
    (-> funs reader/read-string)))

(defn functions!
  ([cb]
   (go (-> `(~'->> ~'*ns* ~'ns-interns ~'keys (~'into [])) eval! <! :val parse-functions cb)))
  ([namespace cb]
   (go (-> `(~'->> '~(symbol namespace) ~'ns-publics ~'keys (~'into [])) eval! <! :val parse-functions cb))))
