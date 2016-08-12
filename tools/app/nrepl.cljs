(ns app.nrepl
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]))

(def ^:private Server
  (-> (.resolve nodejs/require "nrepl-client")
      (.replace "nrepl-client.js" "nrepl-server.js")
      (nodejs/require)))

(def ^:private tree-kill (nodejs/require "tree-kill"))

(def ^:private port 31339)

(def *server-options* (atom {:verbose true
                             ;; TODO Use non-constant port number
                             :port port
                             :projectPath (.tmpdir (nodejs/require "os"))}))

(def ^:private *server* (atom nil))
(def ^:private *server-starting* (atom false))

(defn- server-js->clj [srv]
  ;; srv == {proc: PROCESS, hostname: STRING, port: NUMBER, started: BOOL, exited: BOOL, timedout: BOOL}
  {:hostname (.-hostname srv)
   :port (.-port srv)
   :started (.-started srv)
   :exited (.-exited srv)
   :timedout (.-timedout srv)
   })

(defn- server-options [opts]
  (-> @*server-options*
      (merge opts)
      (clj->js)))

(defn server! []
  (server-js->clj @*server*))

(defn server-present? [] (not (nil? @*server*)))

(defn- launch-server [opts cb] (if-not (server-present?)
                                 (do
                                   (reset! *server-starting* true)
                                   (.start Server (server-options opts)
                                           (fn [err serv]
                                             (if-not (nil? err) (util/error! "nREPL start error: " err)
                                                     (do
                                                       (reset! *server-starting* false)
                                                       (reset! *server* serv)
                                                       (cb (server!)))))))
                                 ;; TODO: handle timed out or dead server
                                 (cb (server!))))


(defn launch! [opts cb] (launch-server opts cb))

(defn kill! [cb]
  (let [server @*server*
        pid (-> server .-proc .-pid)
        sig "SIGKILL"]
    (if (server-present?)
      (do
        (util/log! "Killing REPL with " {:sig sig :pid pid})
        (tree-kill pid sig)
        (reset! *server* nil)
        (cb {:was-running? true
             :is-starting? @*server-starting*}))
      (do
        (util/error! "nREPL not launched")
        (cb {:was-running? false
             :is-starting? @*server-starting*})))))
