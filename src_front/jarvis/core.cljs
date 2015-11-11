(ns jarvis.core
  (:require [figwheel.client :as fw :include-macros true]
            [jarvis.app]
            [reagent.core :as reagent :refer [atom]]))

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback 'mount-root)

(enable-console-print!)

(defn mount-root []
  (reagent/render [jarvis.app.main]
                  (.getElementById js/document "app")))

(defn init! []
  (mount-root))

(init!)
