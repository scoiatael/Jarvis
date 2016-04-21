(ns jarvis.views.code
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [reagent.core :refer [atom]]
            [jarvis.syntax.core :as sc]
            [jarvis.syntax.walk :as walk]
            [jarvis.views.components.code_boxes :as r]
            [jarvis.util.core :as util]))

(defonce ^:private *introspect* (atom false))

(defn- code [id pasting? item index]
  (let [item-to-show (if @*introspect* (->> item walk/normalize sc/parse) item)]
    [rc/border
     :border (str "1px dashed " "transparent")
     :child [r/render
             {:on-click #(dispatch [:node-clicked %1 %2])
              :path []
              :paster pasting?
              :id id
              :on-hover #(dispatch [:node-hover %1 %2])}
             item-to-show]]))

(defn- code-list [id pasting? codes]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed (fn [index item] [code id pasting? item index]) codes)])

(defn- code-boxes [pasting? defs scratch]
  [v-box
   :gap "1em"
   :children [[code-list :defs pasting? defs]
              [rc/line]
              [code-list :scratch pasting? scratch]]])

(defn render []
  (let [pasting? (subscribe [:pasting?])
        defs (subscribe [:defs])
        scratch (subscribe [:scratch])]
    (fn []
      [code-boxes @pasting? @defs @scratch])))
