(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box]]))

(defonce state (atom {:message "Hello Reagent world"}))

(defn root-component []
  [v-box
   :height "inherit"
   :children [[box :child
               [:h1 (:message @state)]]

              [h-box
               :height "300px"
               :children [[box :size "200px" :child "Nav"]
                          [box :size "1" :child "Content"]]]]])

(defn main [] [root-component] )
