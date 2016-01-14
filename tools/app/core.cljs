(ns app.core
  (:require [cljs.nodejs :as nodejs]
            [app.util :as util]
            [app.nrepl :as nrepl]))

(def path (nodejs/require "path"))

(def electron (nodejs/require "electron"))

(def BrowserWindow (.-BrowserWindow electron))

;; (def crash-reporter (.-crashReporter electron))

(def app (.-app electron))

(def *win* (atom nil))

(defn -main []
  ;; (.start crash-reporter)

  (nrepl/launch! (fn [srv]))

  ;; error listener
  (.on nodejs/process "error"
    (fn [err] (.log js/console err)))

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
       (reset! *win* (BrowserWindow. (clj->js {:width 800 :height 600})))

       ;; when no optimize comment out
       (.loadUrl @*win* (str "file://" (.resolve path (js* "__dirname") "../index.html")))

       (.on @*win* "closed" (fn [] (reset! *win* nil))))))

(nodejs/enable-util-print!)

(set! *main-cli-fn* -main)
