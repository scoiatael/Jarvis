(ns jarvis.views.scratch
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [jarvis.views.components.sexp :as r]
            [jarvis.views.components.paster :as ps]
            [jarvis.util.core :as util]))

(defn- clicked [elem path]
  (dispatch [:node-clicked elem path]))

(defn- hover [type elem path]
  (dispatch [:node-hover type elem path]))

(defn- code [item index h]
  (let [id :scratch
        pasting? (:pasting h)]
    [r/render
     {:on-click clicked
      :path []
      :paster pasting?
      :id id
      :on-hover hover}
     item]))

(defn- code-list [codes & args]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed #(into [code %2 %1] args) codes)])

(defn scratch-view [pasting? codes]
  [v-box
   :style {:max-width "100%"}
   :children
   [[code-list codes {:pasting pasting?}]
    (when pasting?
      [ps/big
       :on-click #(dispatch [:scratch-paster-clicked])])]])

(defn render []
  (let [pasting? (subscribe [:pasting?])
        scratch (subscribe [:scratch])]
    (fn []
      [scratch-view @pasting? (reverse @scratch)])))
