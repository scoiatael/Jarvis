(ns jarvis.views.defs
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [jarvis.views.components.sexp :as r]
            [jarvis.util.core :as util]))

(defn- clicked [elem path]
  (let [[elem path] (if (< 1 (count path))
                      [(nth path 1) [(first path)]]
                      [elem path])]
    (dispatch [:def-clicked elem path])))

(defn- hover [type elem path]
  (let [elem (if (< 1 (count path))
               (nth path 1)
               elem)]
    (dispatch [:node-hover type elem])))

(defn- code [item index]
  [rc/box
   :style {:margin "0.5em"}
   :child
   [r/render
    {:on-click clicked
     :path []
     :concise true
     :id :defs
     :on-hover hover}
    item]])

(defn- code-list [codes]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed #(vector code %2 %1) codes)])

(defn render []
  (let [defs (subscribe [:defs])]
    (fn []
      [code-list (reverse @defs)])))
