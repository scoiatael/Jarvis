(ns jarvis.core
  (:require [figwheel.client :as fw :include-macros true]
            [jarvis.app :as app]
            [reagent.core :as reagent]
            [goog.style]))

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback 'mount-root)

(enable-console-print!)

(defn mount-root []
  (reagent/render [jarvis.app.main]
                  (.getElementById js/document "app")))

(defn init! []
  (goog.style/installStyles (app/styles))
  (mount-root))

(init!)
