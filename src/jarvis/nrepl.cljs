(ns jarvis.nrepl
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util :as util]))

(def ^:private Client (nodejs/require "nrepl-client"))

;; (def ^:private Server (nodejs/require "nrepl-client/nrepl-server"))
(def ^:private Server
  (-> (.resolve nodejs/require "nrepl-client")
      (.replace "nrepl-client.js" "nrepl-server.js")
      (nodejs/require)))


(def ^:private *connection* (atom nil))

(def ^:private *server* (atom nil))

(def *server-options* (atom {:verbose true
                             ;; TODO Find better tmp path
                             :projectPath "/tmp/l"}))

(defn server-present? [] (not (= @*server* nil)))

(defn- connect-to-server [server cb]
  (reset! *connection* (.connect Client (clj->js {:port (.-port server)})))
  (.once @*connection* "connect" (clj->js cb)))

(defn- launch-server [cb] (if-not (server-present?)
                         (.start Server (clj->js @*server-options*)
                                 (fn [err serv]
                                   (if-not (nil? err) (util/error! "nREPL start error: " err)
                                           (cb serv))
                                   (reset! *server* serv)))
                         ;; TODO: handle timed out or dead server
                         (util/error! "nREPL already started!")))

(defn- handler [ev err res]
  (if-not (nil? err) (util/error! "nREPL error: " err))
  (util/log! "nREPL response: " res)
  (.send (.-sender ev) "done" (clj->js {})))

(defn launch! [cb] (launch-server #(connect-to-server % cb)))

(defn kill! [cb]
  (let [server @*server*]
    (if (server-present?)
      (.stop Server server (fn [err]
                             (reset! *server* nil)
                             (if-not (nil? err)
                               (util/error! "nREPL kill error:" err)
                               (cb))))
      ;; TODO: handle timed out or dead server
      (util/error! "nREPL not launched"))))

(defn eval! [expr ev] (let [connection @*connection*]
                        (util/log! "Eval: " expr)
                        (if (nil? connection)
                          (util/error! "No connection to nREPL!")
                          (.eval connection expr (partial handler ev)))))

(defn open! [file ev]
  (util/log! "Opening: " file)
  (eval! `(~'load-file ~file) ev))
