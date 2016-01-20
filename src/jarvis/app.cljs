(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
            [reagent.core :refer [atom]]
            [garden.core :refer [css]]
            [jarvis.lifecycle :as lifecycle]
            [jarvis.state :as s]
            [jarvis.syntax.types :as t]
            [jarvis.syntax.pretty-print :as pp]
            [jarvis.font :as font]
            [jarvis.colors.solarized :as sol]
            [jarvis.render :as r]))

(defonce ^:private *introspect* (atom false))
(defonce ^:private *show-code-box* (atom false))
(defonce ^:private *show-right-side* (atom false))

(defn- edit-elem [elem index]
  [input-textarea
   :on-change #(do
                 (lifecycle/push-code % index)
                 (if (nil? (s/error!)) (s/reset-modal)))
   :model (pp/pp elem)
   :width "inherit"])

(defn- render-code [active item index]
  (let [rendered (r/render {:on-hover #(s/set-active index)
                            :on-click #(s/set-modal [item index])}
                           (if @*introspect*
                             (t/parse item)
                             item))]
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
   :backdrop-on-click s/reset-error])

(defn- render-modal [modal]
  [rc/modal-panel :child (apply edit-elem modal)
   :wrap-nicely? true
   :backdrop-color "#666666"
   :backdrop-opacity 0.4
   :backdrop-on-click s/reset-modal])

(defn- main-component []
  (let [codes (s/nodes!)
        active (s/active!)]
    [h-box
     :style { :height "100%" }
     :children [[box :size "200px" :child "Nav"]

                [box
                 :size "1"
                 :child [render-codes codes active]]

                 [v-box
                  :children [[gap :size "1"]

                             [rc/md-circle-icon-button
                              :md-icon-name "zmdi-plus"
                              :on-click lifecycle/add-new-node]

                             [rc/md-circle-icon-button
                              :md-icon-name "zmdi-minus"
                              :on-click lifecycle/pop-code
                              :disabled? (< (count codes) 1)]]]

                [gap
                 :size "1em"]


                (let [code (s/code!)]
                  (if (and @*show-right-side* (not= nil? code))
                    [v-box
                     :size "1"
                     :style {:font-family font/code}
                     :children [[box
                                 :child [:div (pp/pretty-print @s/state)]]

                                [gap :size "2em"]

                                [box
                                 :child [:div (-> code t/parse pp/pretty-print)]]]]))]]))

(defn root-component []
  (let [modal (s/modal!)
        error (s/error!)
        active (s/active!)
        code (s/code!)]
    [v-box
     :height "inherit"
     :children [[main-component]

                [gap :size "1"]

                (if (and @*show-code-box* (not= nil code))

                  [box
                   :width "inherit"
                   :child (edit-elem code active)])

                (if-not (nil? error)
                  (render-error error)

                  (if-not (nil? modal)
                    (render-modal modal)))]]))

(defn styles []
  (css [:body
        {:font-family font/main
         :font-size "medium"
         :background-color sol/base2
         :padding-top "1em"
         :color sol/base03}]))

(defn main [] [root-component] )
