(ns jarvis.views.circle-controllers
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- circle-controllers [allowed?]
  (let [disabled? (fn [x] (not (allowed? x)))]
    [v-box
     :children [[rc/md-circle-icon-button
                 :md-icon-name "zmdi-plus"
                 :on-click #(dispatch [:icon-plus-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-delete"
                 :on-click #(dispatch [:icon-delete-clicked])
                 :disabled? (disabled? :delete)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-undo"
                 :on-click #(dispatch [:icon-undo-clicked])
                 :disabled? (disabled? :undo)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-file-text"
                 :on-click #(dispatch [:icon-file-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-minus"
                 :on-click #(dispatch [:icon-minus-clicked])
                 :disabled? (disabled? :minus)]]]))

(defn render []
  (let [pasting? (subscribe [:pasting?])
        can-remove? (subscribe [:can-remove?])
        can-undo? (subscribe [:can-undo?])]
    (fn []
      [circle-controllers {:delete @pasting?
                           :undo @can-undo?
                           :minus @can-remove?}])))
