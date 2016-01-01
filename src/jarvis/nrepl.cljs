(ns jarvis.nrepl
  (:require [cljs.nodejs :as nodejs]))

(def ^:private Client (nodejs/require "nrepl-client"))

;; (def ^:private Server (nodejs/require "nrepl-client/nrepl-server"))
(def ^:private Server
  (-> (.resolve nodejs/require "nrepl-client")
      (.replace "nrepl-client.js" "nrepl-server.js")
      (nodejs/require)))


(def ^:private *connection* (atom nil))

(def ^:private *server* (atom nil))

(def *server-options* (atom {:verbose true}))

(defn server-present? [] (not (= @*server* nil)))

(defn- connect-to-server [server cb]
  (reset! *connection* (.connect Client (clj->js {:port (.-port server)})))
  (.once @*connection* "connect" (clj->js cb)))

(defn- launch-server [cb] (if-not (server-present?)
                         (.start Server (clj->js @*server-options*)
                                 (fn [err serv]
                                   (if-not (= nil err) (.error js/console "nREPL start error: " err)
                                           (cb serv))
                                   (reset! *server* serv)))
                         ;; TODO: handle timed out or dead server
                         (.error js/console "nREPL already started!")))

(defn- handler [err res]
  (if-not (= nil err) (.error js/console "nREPL eval error: " err))
  (.log js/console "nREPL response: " res))

(defn launch! [cb] (launch-server #(connect-to-server % cb)))

(defn eval! [expr] (let [connection @*connection*]
                    (if (= nil connection)
                      (.error js/console "No connection to nREPL!")
                      (.eval connection expr handler))))

(defn kill! [cb]
  (let [server @*server*]
    (if (server-present?)
      (.stop Server server (fn [err]
                             (reset! *server* nil)
                             (if-not (= nil err)
                               (.error js/console "nREPL kill error:" err)
                               (cb))))
      ;; TODO: handle timed out or dead server
      (.error js/console "nREPL not launched"))))
