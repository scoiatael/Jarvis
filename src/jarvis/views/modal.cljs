(ns jarvis.views.modal
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- edit-elem []
  [input-textarea
   :on-change #(dispatch [:edit-elem-changed %])
   :model ""
   :width "inherit"])

(defn- modal-panel []
  [rc/modal-panel
   :child [edit-elem]
   :wrap-nicely? true
   :backdrop-color "#666666"
   :backdrop-opacity 0.4
   :backdrop-on-click #(dispatch [:modal-backdrop-clicked])])

(defn render []
  (let [modal (subscribe [:modal])]
    (fn []
      (when @modal
        [modal-panel]))))
