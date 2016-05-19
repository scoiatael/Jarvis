(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [app.util :as util]
            [app.ipc :as ipc]
            [app.nrepl :as nrepl]))

(def path (nodejs/require "path"))

(def electron (nodejs/require "electron"))

(def BrowserWindow (.-BrowserWindow electron))

(def app (.-app electron))

(def *win* (atom nil))

(defn -main []
  (util/log! "Starting up..")

  (.on ipc/main "start-server"
       (fn [ev arg]
         (nrepl/launch! {}
                        (partial ipc/reply! ev "server-started"))))

  (.on ipc/main "kill-server"
       (fn [ev arg]
         (nrepl/kill!
          (partial ipc/reply! ev "server-killed"))))

  ;; error listener
  (.on nodejs/process "error"
    (fn [err] (.error js/console err)))

  ; window all closed listener
   (.on app "window-all-closed"
     (fn [] (if (not= (.-platform nodejs/process) "darwin")
              (.quit app))))

  (.on app "will-quit"
       (fn [ev] (do
                 (util/log! "Quitting, killing repl? ->", (nrepl/server-present?))
                 (if (nrepl/server-present?)
                   (do
                     (.preventDefault ev)
                     (nrepl/kill! #(.quit app)))))))

  ; ready listener
   (.on app "ready"
     (fn []
       (reset! *win* (BrowserWindow. (clj->js {:fullscreen true :title "Jarvis"})))

       ;; when no optimize comment out
       (.loadURL @*win* (str "file://" (.resolve path (js* "__dirname") "../index.html")))

       (.on @*win* "closed" (fn [] (reset! *win* nil))))))

(nodejs/enable-util-print!)

(set! *main-cli-fn* -main)
