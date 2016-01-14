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

(defn server-present? [] (not (nil? @*server*)))

(defn- launch-server [cb] (if-not (server-present?)
                            (.start Server (clj->js @*server-options*)
                                    (fn [err serv]
                                      (if-not (nil? err) (util/error! "nREPL start error: " err)
                                              (cb serv))
                                      (reset! *server* serv)))
                            ;; TODO: handle timed out or dead server
                            (util/error! "nREPL already started!")))


(defn launch! [cb] (launch-server cb))

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
