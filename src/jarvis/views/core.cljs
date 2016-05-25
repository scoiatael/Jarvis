(ns jarvis.views.core
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [garden.core :refer [css]]
            [jarvis.views.font :as font]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.util.core :as util]
            [jarvis.views.scratch :as scratch]
            [jarvis.views.defs :as defs]
            [jarvis.views.circle-controllers :as circle-controllers]
            [jarvis.views.suggestions :as suggestions]
            [jarvis.views.status-bar :as status-bar]
            [jarvis.views.modal :as modal]
            [jarvis.views.error :as error]))

(defn- main-component []
  [h-box
   :style { :height "100%" }
   :gap "1em"
   :children [[box
               :size "1"
               :child [defs/render]]

              [circle-controllers/render]

              ;; [box
              ;;  :size "20em"
              ;;  :child [suggestions/render]]

              [box
               :size "1"
               :child [scratch/render]]]])

(defn main []
  [v-box
   :height "inherit"
   :children [[status-bar/render]

              [main-component]

              [modal/render]
              [error/render]]])

(def styles
  (css [:body
        {:font-family font/main
         :font-size "medium"
         :background-color sol/base2
         :padding-top "1em"
         :color sol/base03}]))
