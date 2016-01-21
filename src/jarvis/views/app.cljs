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
(defonce ^:private *show-right-side* (atom true))

(defn- edit-elem [state modal]
  (let [index (last modal)
        elem (first modal)]
    [input-textarea
     :on-change #(do
                   (lifecycle/push-code % index)
                   (if (nil? (s/error state)) (lifecycle/reset-modal)))
     :model (pp/pp elem)
     :width "inherit"]))

(defn- render-code [active item index]
  (let [rendered (r/render {:on-hover #(lifecycle/set-active index)
                            :on-click #(lifecycle/set-modal [item index])}
                             item)]
      [rc/border
       :border (str "1px dashed " (if (= active index) sol/red "transparent"))
       :child rendered]))

(defn- render-codes [codes active]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed (fn [index item] [render-code active item index]) codes)])

(defn- render-error [error]
  [rc/modal-panel :child (.-message error)
   :wrap-nicely? true
   :backdrop-color sol/red
   :backdrop-opacity 0.4
   :backdrop-on-click lifecycle/reset-error])

(defn- render-modal [state modal]
  [rc/modal-panel :child (apply edit-elem [state modal])
   :wrap-nicely? true
   :backdrop-color "#666666"
   :backdrop-opacity 0.4
   :backdrop-on-click lifecycle/reset-modal])

(defn- render-state-code [state code]
  (if (satisfies? walk/Info code)
    (let [parsed-code (->> code walk/normalize sc/parse)]
      (util/log! parsed-code)
      [v-box
       :size "1"
       :style {:font-family font/code}
       :children [[box
                   :child [r/render {} parsed-code]]]])))

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

(defn- main-component [state]
  (let [codes (s/nodes state)
        active (s/active state)
        code (s/code state)]
    [h-box
     :style { :height "100%" }
     :children [[box :size "200px" :child "Nav"]

                [box
                 :size "1"
                 :child [render-codes codes active]]

                [render-circle-controllers codes]

                [gap
                 :size "1em"]

                  (if (and @*show-right-side* (not= nil? code))
                    [render-state-code state code])]]))

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
                    (render-modal state modal)))]]))

(defn styles []
  (css [:body
        {:font-family font/main
         :font-size "medium"
         :background-color sol/base2
         :padding-top "1em"
         :color sol/base03}]))
