(ns jarvis.views.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
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
            [jarvis.util.logger :as util]))

(defonce ^:private *show-code-box* (atom false))
(defonce ^:private *introspect* (atom false))

(defn- edit-elem [state]
  [input-textarea
   :on-change #(do
                 (lifecycle/push-code %)
                 (if (nil? (s/error state)) (lifecycle/reset-modal)))
   :model ""
   :width "inherit"])

(defn- render-code [item index]
  (let [item-to-show (if @*introspect* (->> item walk/normalize sc/parse) item)]
      [rc/border
       :border (str "1px dashed " "transparent")
       :child [r/render
               ;; {:on-click #(lifecycle/mark %1)}
               {:on-hover #(if (= :over %1)
                             (lifecycle/mark %2)
                             (lifecycle/unmark %2))}
               item-to-show]]))

(defn- render-codes [codes]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed (fn [index item] [render-code item index]) codes)])

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
   :children [[gap :size "1"]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-plus"
               :on-click lifecycle/add-new-node]

              [rc/md-circle-icon-button
               :md-icon-name "zmdi-minus"
               :on-click lifecycle/pop-code
               :disabled? (< (count codes) 1)]]])

(defn- render-namespace [name functions]
  [v-box
   :children (map (fn [item] (str item)) functions)])

(defn- render-suggestions [suggestions]
  [v-box
   :children (map (fn [item] [render-namespace (first item) (last item)]) suggestions)])

(defn- main-component [state]
  (let [codes (s/nodes state)
        suggestions (s/suggestions state)]
    [h-box
     :style { :height "100%" }
     :children [[box :size "200px" :child [render-suggestions suggestions]]

                [box
                 :size "1"
                 :child [render-codes codes]]

                [render-circle-controllers codes]]]))

(defn main [state-getter]
  (let [state (state-getter)
        modal (s/modal state)
        error (s/error state)]
    [v-box
     :height "inherit"
     :children [[main-component state]

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
