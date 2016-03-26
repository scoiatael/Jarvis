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

(defn- status-bar [state]
  [box
   :style {:background-color sol/green}
   :child [:div "Status bar"]])

(defn render []
  (let [status (subscribe [:status])]
    (fn []
      [status-bar status])))
