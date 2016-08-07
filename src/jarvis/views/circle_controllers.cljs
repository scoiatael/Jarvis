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
                 :md-icon-name "zmdi-play"
                 :on-click #(dispatch [:icon-play-clicked])
                 :disabled? (disabled? :play)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-file-text"
                 :on-click #(dispatch [:icon-file-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-archive"
                 :on-click #(dispatch [:icon-save-clicked])]]]))

(defn render []
  (let [pasting? (subscribe [:pasting?])
        can-undo? (subscribe [:can-undo?])]
    (fn []
      [circle-controllers {:delete @pasting?
                           :play @pasting?
                           :undo @can-undo?}])))
