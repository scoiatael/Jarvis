(ns jarvis.views.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [reagent.core :refer [atom]]
            [garden.core :refer [css]]
            [jarvis.lifecycle :as lifecycle]
            [jarvis.state.helpers :as s]
            [jarvis.syntax.core :as sc]
            [jarvis.syntax.pretty-print :as pp]
            [jarvis.views.font :as font]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.views.render :as r]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.core :as util]))

(defonce ^:private *show-code-box* (atom false))
(defonce ^:private *introspect* (atom false))

(defn- edit-elem [state]
  [input-textarea
   :on-change #(do
                 (lifecycle/push-code %)
                 (if (nil? (s/error state)) (lifecycle/reset-modal)))
   :model ""
   :width "inherit"])

(defn- render-code [pasting? item index]
  (let [item-to-show (if @*introspect* (->> item walk/normalize sc/parse) item)]
      [rc/border
       :border (str "1px dashed " "transparent")
       :child [r/render
               {:on-click #(do
                             ;; (util/log! %1 %2)
                             (if pasting? (lifecycle/paste-node %2 %1) (lifecycle/cut-node %2 %1)))
                :path []
                :paster pasting?
                :id 0 ;; FIXME: nodes_map root... ugly constant.
                :on-hover #(if (= :over %1)
                             (lifecycle/mark %2)
                             (lifecycle/unmark %2))}
               item-to-show]]))

(defn- render-codes [pasting? codes]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed (fn [index item] [render-code pasting? item index]) codes)])

(defn- render-error [error]
  [rc/modal-panel :child (.-message error)
   :wrap-nicely? true
   :backdrop-color sol/red
   :backdrop-opacity 0.4
   :backdrop-on-click lifecycle/reset-error])

(defn- render-modal [state]
  [rc/modal-panel
   :child [edit-elem state]
   :wrap-nicely? true
   :backdrop-color "#666666"
   :backdrop-opacity 0.4
   :backdrop-on-click lifecycle/reset-modal])

(defn- render-circle-controllers [codes]
  [v-box
   :children [[rc/md-circle-icon-button
               :md-icon-name "zmdi-plus"
               :on-click lifecycle/add-new-node]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-delete"
               :on-click lifecycle/delete]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-undo"
               :on-click lifecycle/undo]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-file-text"
               :on-click lifecycle/open-file]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-minus"
               :on-click lifecycle/pop-code
               :disabled? (< (count codes) 1)]]])

(defn- on-suggested-fn-chosen [ns fn]
  (util/log! "got" ns "/" fn)
  (lifecycle/push-code (str ns "/" fn)))

(defn- render-namespace [name functions]
  (let [on-click #(on-suggested-fn-chosen name %)
        render-function (fn [fn]
                          [box
                           :child (str fn)
                           :attr {:on-click (partial util/dont-bubble #(on-click fn))}])]
    [v-box :children
     [[:div [:b name]]
      (if (< (count functions) 4)
        [h-box
         :children [[box :style {:width "1em"} :child [:div]]
                    [v-box
                     :children (map render-function functions)]]]
        [single-dropdown
         :filter-box? true
         :width "100%"
         :choices (into [] (map-indexed (fn [id it] {:id id :label (str it)}) functions))
         :model nil
         :placeholder "Search for one"
         :on-change #(do (on-click (nth functions %)))])]]))

(defn- render-suggestions [suggestions]
  [v-box
   :children (->> suggestions
                  (sort-by first)
                  (map (fn [item] [render-namespace (first item) (last item)])))
   :style {:width "100%"}])

(defn- main-component [state]
  (let [pasting? (s/pasting? state)
        codes (s/nodes state)
        suggestions (s/suggestions state)]
    [h-box
     :style { :height "100%" }
     :gap "1em"
     :children [[render-circle-controllers codes]

                [box :size "200px" :child [render-suggestions suggestions]]

                [box
                 :size "1"
                 :child [render-codes pasting? codes]]]]))

(defn- status-bar [state]
  [box
   :style {:background-color sol/green}
   :child [:div "Status bar"]])

(defn main [state-getter]
  (let [state (state-getter)
        modal (s/modal state)
        error (s/error state)]
    [v-box
     :height "inherit"
     :children [[status-bar state]

                [main-component state]

                (if-not (nil? error)
                  (render-error error)

                  (if-not (nil? modal)
                    (render-modal state)))]]))

(defn styles []
  (css [:body
        {:font-family font/main
         :font-size "medium"
         :background-color sol/base2
         :padding-top "1em"
         :color sol/base03}]))
