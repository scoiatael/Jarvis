(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.core :as util]
            [app.ipc :as ipc]
            [app.nrepl :as nrepl]
            [app.menu :as menu]))

(def path (nodejs/require "path"))

(def electron (nodejs/require "electron"))

(def BrowserWindow (.-BrowserWindow electron))

(def app (.-app electron))

(def *win* (atom nil))

(defn ^:export -main []
  (util/log! "Starting up..")

  (.on ipc/main "start-server"
       (fn [ev arg]
         (nrepl/launch! {}
                        (fn [srv]
                          (ipc/reply! ev "server-started" srv)))))

  (.on ipc/main "kill-server"
       (fn [ev arg]
         (nrepl/kill!
          #(ipc/reply! ev "server-killed" {}))))

  (.on ipc/main "restart-server"
       (fn [ev arg]
         (util/log! "Restarting server..")
         (nrepl/kill!
          #(do
             (util/log! "Killed server, starting..")
             (ipc/reply! ev "server-killed" {})
             (if-not (:is-starting? %)
               (nrepl/launch! {}
                              (fn [srv]
                                (ipc/reply! ev "server-started" srv))))))))

  ;; error listener
  (.on nodejs/process "error"
       (fn [err]
         (.error js/console err)
         (.quit app)))

  ; window all closed listener
   (.on app "window-all-closed"
     (fn [] (if (not= (.-platform nodejs/process) "darwin")
              (.quit app))))

  (.on app "will-quit"
       (fn [ev] (do
                 (util/log! "Quitting")
                 (if (nrepl/server-present?)
                   (do
                     (.preventDefault ev)
                     (nrepl/kill! #(.quit app)))))))

  ; ready listener
   (.on app "ready"
     (fn []
       (menu/init)

       (reset! *win* (BrowserWindow. (clj->js {:title "Jarvis"})))

       (.loadURL @*win* (str "file://" (.resolve path (js* "__dirname") "index.html")))

       (.on @*win* "closed" (fn [] (reset! *win* nil))))))

(set! *main-cli-fn* -main)
