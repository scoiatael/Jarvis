(ns jarvis.core
  (:require [figwheel.client :as fw :include-macros true]
            [jarvis.views.core :as app]
            [jarvis.util.nrepl :as nrepl]
            [jarvis.util.ipc :as ipc]
            [jarvis.util.logger :as util]
            [re-frame.core :as r-f]
            [jarvis.subs :as subs]
            [jarvis.handlers :as handlers]
            [reagent.core :as reagent]
            [goog.style]
            [cljs.nodejs :as nodejs]))

(def electron (nodejs/require "electron"))

(def app (.-app electron))

(enable-console-print!)

(defn mount-root []
  (subs/register!)
  (handlers/register!)

  (reagent/render [app/main]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (goog.style/installStyles app/styles)

  (.once ipc/renderer "server-started"
         (fn [srv] (nrepl/connect-to-server
                   (fn []
                     (util/log! "Connected to nREPL")
                     (r-f/dispatch [:repl-connected])))))

  (mount-root)
  (ipc/start-server! {})
  (r-f/dispatch-sync [:initialise-db]))

(when js/goog.DEBUG
  (fw/watch-and-reload
   :websocket-url   "ws://localhost:3449/figwheel-ws"
   :jsload-callback mount-root))
