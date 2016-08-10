(ns jarvis.views.tabs
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown horizontal-tabs] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [jarvis.views.circle-controllers :as circle-controllers]
            [jarvis.views.scratch :as scratch]
            [jarvis.views.defs :as defs]))

(defn- tab-component [tab]
  [h-box
   :style { :height "100%" }
   :gap "1em"
   :children [[circle-controllers/render]
              (case tab
                :edit [scratch/render]
                :eval [defs/render]
                [:div "Unknown component.."])]])

(defn- tabs [tab]
  {:pre [#(#{:edit :eval} tab)]}
  [horizontal-tabs
   :tabs (map #(hash-map :id % :label (name %)) [:edit :eval])
   :model tab
   :on-change #(dispatch [:tab-clicked %])])

(defn render []
  (let [tab (subscribe [:tab])]
    (fn []
      [v-box
       :style { :height "100%" }
       :gap "1em"
       :children (if @tab
                   [[tabs @tab]
                    [tab-component @tab]]
                   [:div "Loading..."])])))
