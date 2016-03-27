(ns jarvis.views.status-bar
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [reagent.core :refer [atom]]
            [garden.core :refer [css]]
            [jarvis.state.core :as s]
            [jarvis.syntax.core :as sc]
            [jarvis.syntax.pretty-print :as pp]
            [jarvis.views.font :as font]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.views.components.code_boxes :as r]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.core :as util]
            [jarvis.views.code :as code]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- nrepl-not-connected []
  [box
   :size "auto"
   :style {:background-color sol/red}
   :child [:div "Connecting to nREPL..."]])

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
