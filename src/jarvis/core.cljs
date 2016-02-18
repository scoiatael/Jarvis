(ns jarvis.core
  (:require [figwheel.client :as fw :include-macros true]
            [jarvis.views.app :as app]
            [jarvis.state.core :as state]
            [jarvis.util.nrepl :as nrepl]
            [jarvis.util.ipc :as ipc]
            [jarvis.util.logger :as util]
            [jarvis.util.bus :as bus]
            [reagent.core :as reagent]
            [goog.style]
            [cljs.nodejs :as nodejs]))

(def electron (nodejs/require "electron"))

(def app (.-app electron))

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback 'mount-root)

(enable-console-print!)

(defn mount-root []
  (reagent/render [app/main state/fetch]
                  (.getElementById js/document "app")))

(defn init! []
  (goog.style/installStyles (app/styles))

  (.once ipc/renderer "server-started"
         (fn [srv] (nrepl/connect-to-server
                   (fn [] (util/log! "Connected to nREPL")))))

  (util/log! "Requesting nREPL start..")
  (ipc/start-server! {})

  (mount-root))

(init!)
