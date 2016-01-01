(ns jarvis.core
  (:require [cljs.nodejs :as nodejs]
            [jarvis.nrepl :as nrepl]))

(def path (nodejs/require "path"))

(def BrowserWindow (nodejs/require "browser-window"))

(def crash-reporter (nodejs/require "crash-reporter"))

(def *win* (atom nil))

(def app (nodejs/require "app"))

(defn -main []
  (.start crash-reporter)

  ;; error listener
  (.on nodejs/process "error"
    (fn [err] (.log js/console err)))

  ; window all closed listener
   (.on app "window-all-closed"
     (fn [] (if (not= (.-platform nodejs/process) "darwin")
              (.quit app))))

  ; ready listener
   (.on app "ready"
     (fn []
       (reset! *win* (BrowserWindow. (clj->js {:width 800 :height 600})))

       ;; when no optimize comment out
       (.loadUrl @*win* (str "file://" (.resolve path (js* "__dirname") "../index.html")))
       ;; when no optimize uncomment
       ;; (.loadUrl @*win* (str "file://" (.resolve path (js* "__dirname") "../../../index.html")))

       (.on @*win* "closed" (fn [] (reset! *win* nil))))))

(nodejs/enable-util-print!)

(set! *main-cli-fn* -main)

(nrepl/launch! #(.log js/console "Connected to nREPL"))

(.on app "will-quit"
     (fn [ev] (if (nrepl/server-present?)
               (do
                 (.preventDefault ev)
                 (nrepl/kill! #(.quit app))))))


(.on nodejs/process "SIGUSR1" (fn []
                                (.log js/console "SIGUSR1 detected")
                                (nrepl/eval! "(+ 1 10)")))
