(ns jarvis.views.code
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [reagent.core :refer [atom]]
            [jarvis.syntax.core :as sc]
            [jarvis.syntax.walk :as walk]
            [jarvis.views.components.code_boxes :as r]
            [jarvis.util.core :as util]))

(defonce ^:private *introspect* (atom false))

(defn- code [h item index]
  (let [id (:id h)
        pasting? (:pasting h)]
    (let [item-to-show (if @*introspect* (->> item walk/normalize sc/parse) item)]
      [rc/border
       :border (str "1px dashed " "transparent")
       :child [r/render
               {:on-click #(dispatch [:node-clicked %1 %2])
                :path []
                :paster pasting?
                :id id
                :on-hover #(dispatch [:node-hover %1 %2])}
               item-to-show]])))

(defn- code-list [h codes]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed (fn [index item] [code h item index]) codes)])

(defn- code-boxes [pasting? defs scratch]
  (let [h {:pasting pasting?}]
    [v-box
     :gap "1em"
     :children [[code-list (assoc h :id :defs :pasting false) defs]
                [rc/line]
                [code-list (assoc h :id :scratch) scratch]]]))

(defn render []
  (let [pasting? (subscribe [:pasting?])
        defs (subscribe [:defs])
        scratch (subscribe [:scratch])]
    (fn []
      [code-boxes @pasting? (reverse @defs) (reverse @scratch)])))
