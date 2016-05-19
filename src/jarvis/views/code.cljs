(ns jarvis.views.code
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [reagent.core :refer [atom]]
            [jarvis.syntax.core :as sc]
            [jarvis.syntax.walk :as walk]
            [jarvis.views.components.code_boxes :as r]
            [jarvis.views.components.paster :as past]
            [jarvis.util.core :as util]))

(defn- def-clicked [elem path]
  (let [[elem path] (if (< 1 (count path))
                      [(nth path 1) [(first path)]]
                      [elem path])]
    (dispatch [:node-clicked elem path])))

(defn- def-hover [type elem path]
  (let [elem (if (< 1 (count path))
               (nth path 1)
               elem)]
    (dispatch [:node-hover type elem])))

(defn- def-code [item index]
  [r/render
   {:on-click def-clicked
    :path []
    :id :scratch
    :on-hover def-hover}
   item])

(defn- scratch-clicked [elem path]
  (dispatch [:node-clicked elem path]))

(defn- scratch-hover [type elem]
  (dispatch [:node-hover type elem]))

(defn- scratch-code [item index h]
  (let [id :scratch
        pasting? (:pasting h)]
    [r/render
     {:on-click scratch-clicked
      :path []
      :paster pasting?
      :id id
      :on-hover scratch-hover}
     item]))

(defn- code-list [codes render-fn & args]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed #(into [render-fn %2 %1] args) codes)])

(defn- paster-for [id]
  [past/big
   :on-click #(dispatch [:paster-clicked-for id])
   :tooltip (str "Add node to " id)])

(defn- code-boxes [pasting? defs scratch]
  (let [h {:pasting pasting?}]
    [v-box
     :gap "1em"
     :children [[code-list defs def-code]
                (if pasting? [paster-for :defs])
                [rc/line]
                [code-list scratch scratch-code {:pasting pasting?}]
                (if pasting? [paster-for :scratch])]]))

(defn render []
  (let [pasting? (subscribe [:pasting?])
        defs (subscribe [:defs])
        scratch (subscribe [:scratch])]
    (fn []
      [code-boxes @pasting? (reverse @defs) (reverse @scratch)])))
