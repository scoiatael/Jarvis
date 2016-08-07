(ns jarvis.views.circle-controllers
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- circle-controllers [allowed?]
  (let [disabled? (fn [x] (not (allowed? x)))]
    [v-box
     :children [[rc/md-circle-icon-button
                 :md-icon-name "zmdi-plus"
                 :on-click #(dispatch [:icon-plus-clicked])
                 :disabled? (disabled? :plus)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-file-text"
                 :on-click #(dispatch [:icon-file-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-archive"
                 :on-click #(dispatch [:icon-save-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-play"
                 :on-click #(dispatch [:icon-play-clicked])
                 :disabled? (disabled? :play)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-copy"
                 :on-click #(dispatch [:icon-copy-clicked])
                 :disabled? (disabled? :copy)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-swap"
                 :on-click #(dispatch [:icon-cut-clicked])
                 :disabled? (disabled? :cut)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-delete"
                 :on-click #(dispatch [:icon-delete-clicked])
                 :disabled? (disabled? :delete)]]]))

(defn render []
  (let [context-actions? (subscribe [:context-actions?])
        focus? (subscribe [:focus?])]
    (fn []
      [circle-controllers {:plus (not @context-actions?)
                           :play @context-actions?
                           :copy @focus?
                           :cut @focus?
                           :delete @context-actions?}])))
