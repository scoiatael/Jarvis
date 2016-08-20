(ns jarvis.views.circle-controllers
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- circle-controllers [allowed?]
  (let [disabled? (fn [x] (not (allowed? x)))]
    [v-box
     :children [[rc/md-circle-icon-button
                 :md-icon-name "zmdi-plus"
                 :tooltip "Add element"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-plus-clicked])
                 :disabled? (disabled? :plus)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-file-text"
                 :tooltip "Open file"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-file-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-archive"
                 :tooltip "Save file"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-save-clicked])]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-play"
                 :tooltip "Eval"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-play-clicked])
                 :disabled? (disabled? :play)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-copy"
                 :tooltip "Copy"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-copy-clicked])
                 :disabled? (disabled? :copy)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-swap"
                 :tooltip "Cut"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-cut-clicked])
                 :disabled? (disabled? :cut)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-delete"
                 :tooltip "Delete"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-delete-clicked])
                 :disabled? (disabled? :delete)]

                [rc/md-circle-icon-button
                 :md-icon-name "zmdi-replay"
                 :tooltip "Restart nREPL"
                 :tooltip-position :right-below
                 :on-click #(dispatch [:icon-restart-clicked])
                 :disabled? (disabled? :restart)]]]))

(defn render []
  (let [context-actions? (subscribe [:context-actions?])
        server-started? (subscribe [:status])
        focus? (subscribe [:focus?])]
    (fn []
      [circle-controllers {:plus (not @context-actions?)
                           :play @context-actions?
                           :copy @focus?
                           :cut @focus?
                           :restart (-> @server-started? first nil? not)
                           :delete @context-actions?}])))
