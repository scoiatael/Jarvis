(ns app.nrepl
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]))

(def ^:private Server
  (-> "nrepl-client"
      nodejs/require
      .-Server))

(def *server-options* (atom {:verbose true
                             :logger util/logger
                             :projectPath (.tmpdir (nodejs/require "os"))}))

(def ^:private *server* (atom nil))
(def ^:private *server-starting* (atom false))

(defn- server-js->clj [srv]
  {:hostname (.-host srv)
   :port (.-port srv)})

(defn- server-options [opts]
  (-> @*server-options*
      (merge opts)
      (clj->js)))

(defn server! []
  (server-js->clj @*server*))

(defn server-present? [] (not (nil? @*server*)))

(defn launch! [opts cb] (if-not (server-present?)
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

(defn kill! [cb]
  (let [server @*server*]
    (if (server-present?)
      (do
        (reset! *server* nil)
        (.stop server
               #(cb {:was-running? true
                    :is-starting? @*server-starting*})))
      (do
        (util/error! "nREPL not launched")
        (cb {:was-running? false
             :is-starting? @*server-starting*})))))
