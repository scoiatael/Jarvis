(ns jarvis.views.status-bar
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [jarvis.views.colors.solarized :as sol]
            [re-frame.core :as r-f :refer [subscribe]]))

(defn- nrepl-not-connected []
  [box
   :size "auto"
   :style {:background-color sol/orange-alt}
   :child [h-box
           :children [[:div "Connecting to nREPL..."]
                      [rc/throbber
                       :size :small
                       :color sol/blue
                       :style {:height "1em"}]]]])

(defn- nrepl-status [status]
  [box
   :size "auto"
   :style {:background-color sol/green}
   :child [:div "nREPL connected"]])

(defn- pasting-notification [status]
  [box
   :size "auto"
   :style {:background-color sol/yellow}
   :child [:div "Pasting"]])

(defn- status-bar [[nrepl-connection pasting]]
  (let [children (if-not nrepl-connection
                   [[nrepl-not-connected]]
                   [[nrepl-status nrepl-connection]
                    (when pasting [pasting-notification])])]
    [h-box
     :style {:background-color sol/base01}
     :justify :around
     :children children]))

(defn render []
  (let [status (subscribe [:status])]
    (fn []
      [status-bar @status])))
