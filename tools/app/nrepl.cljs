(ns app.nrepl
  (:require [cljs.nodejs :as nodejs]
            [app.util :as util]))

;; (def ^:private Server (nodejs/require "nrepl-client/nrepl-server"))
(def ^:private Server
  (-> (.resolve nodejs/require "nrepl-client")
      (.replace "nrepl-client.js" "nrepl-server.js")
      (nodejs/require)))

(def ^:private port 31339)

(def *server-options* (atom {:verbose true
                             ;; TODO Use non-constant port number
                             :port port
                             ;; TODO Find better tmp path
                             :projectPath "/tmp/l"}))

(def ^:private *server* (atom nil))

(defn- server-js->clj [srv]
  ;; srv == {proc: PROCESS, hostname: STRING, port: NUMBER, started: BOOL, exited: BOOL, timedout: BOOL}
  {:hostname (.-hostname srv)
   :port (.-port srv)
   :started (.-started srv)
   :exited (.-exited srv)
   :timedout (.-timedout srv)
   })

(defn server! []
  (server-js->clj @*server*))

(defn server-present? [] (not (nil? @*server*)))

(defn- launch-server [opts cb] (if-not (server-present?)
                                 (.start Server (clj->js @*server-options*)
                                         (fn [err serv]
                                           (if-not (nil? err) (util/error! "nREPL start error: " err)
                                                   (do
                                                     (reset! *server* serv)
                                                     (cb (server!))))))
                                 ;; TODO: handle timed out or dead server
                                 (cb (server!))))


(defn launch! [opts cb] (launch-server opts cb))

(defn kill! [cb]
  (let [server @*server*]
    (if (server-present?)
      (.stop Server server (fn [err]
                             (reset! *server* nil)
                             (util/log! "nREPL killed")
                             (if-not (nil? err)
                               (util/error! "nREPL kill error:" err)
                               (cb))))
      ;; TODO: handle timed out or dead server
      (util/error! "nREPL not launched"))))
