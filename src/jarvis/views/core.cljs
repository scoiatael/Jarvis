(ns jarvis.views.core
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [garden.core :refer [css]]
            [jarvis.views.font :as font]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.util.core :as util]
            [jarvis.views.tabs :as tabs]
            [jarvis.views.status-bar :as status-bar]
            [jarvis.views.modal :as modal]
            [jarvis.views.error :as error]))

(defn main []
  [v-box
   :height "inherit"
   :children [[status-bar/render]

              [tabs/render]

              [modal/render]
              [error/render]]])

(def styles
  (css [:body
        {:font-family font/main
         :font-size "medium"
         :background-color sol/base2
         :padding-top "1em"
         :color sol/base03}]))
