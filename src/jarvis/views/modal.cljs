(ns jarvis.views.modal
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]
            [jarvis.views.suggestions :as suggestions]))


(def ^:private *elems* {"(def *var* *value*)" "(def)"
                        "(defn *fun* [*args*] *body*)" "(defn)"
                        "()" "()"
                        "[]" "[]"})

(defn- edit-elem []
  [input-textarea
   :on-change #(dispatch [:edit-elem-changed %])
   :model ""
   :width "inherit"])

(defn- add-elem [[display code]]
  [rc/button
   :label display
   :style {:width "100%"
           :margin "0.1em"}
   :on-click #(dispatch [:add-elem-clicked code])])

(defn- add-elem-panel []
  [v-box
   :children (map add-elem *elems*)])

(defn- edit-elem-panel []
  [v-box
   :children [[box
               :size "20em"
               :child [suggestions/render]]
              [add-elem-panel]
              [edit-elem]]])

(defn- modal-panel []
  [rc/modal-panel
   :child [edit-elem-panel]
   :wrap-nicely? true
   :backdrop-color "#666666"
   :backdrop-opacity 0.4
   :backdrop-on-click #(dispatch [:modal-backdrop-clicked])])

(defn render []
  (let [modal (subscribe [:modal])]
    (fn []
      (when @modal
        [modal-panel]))))
