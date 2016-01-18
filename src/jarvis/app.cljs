(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
            [reagent.core :refer [atom]]
            [garden.core :refer [css]]
            [jarvis.lifecycle :as lifecycle]
            [jarvis.state :as s]
            [jarvis.types :as t]
            [jarvis.font :as font]
            [jarvis.colors.solarized :as sol]
            [jarvis.pretty-print :as pp]
            [jarvis.render :as r]))

(defn- edit-elem [elem index]
  [input-textarea
   :on-change #(do
                 (s/push-code % index)
                 (if (nil? (s/error!)) (s/reset-modal)))
   :model (pp/pretty-print elem)
   :width "inherit"])

(defn- render-code [active item index]
  (let [rendered (r/render {:on-hover #(s/set-active index)
                            :on-click #(s/set-modal [item index])}
                           (-> item t/parse))]
      [rc/border
       ;; :style {:max-width "100%"}
       :border (str "1px dashed " (if (= active index) sol/red "transparent"))
       :child rendered]))

(defn- render-codes [cs a]
  [v-box
   :align :start
   :style {:max-width "100%"}
   :children (map-indexed #(render-code a %2 %1) cs)])

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

(defonce show-right-side (atom false))

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
                              :on-click s/add-empty-node]

                             [rc/md-circle-icon-button
                              :md-icon-name "zmdi-minus"
                              :on-click s/pop-code
                              :disabled? (> 2 (count codes))]]]

                [gap
                 :size "1em"]


                (let [code (s/code!)]
                  (if (and @show-right-side (not= nil? code))
                    [v-box
                     :size "1"
                     :style {:font-family font/code}
                     :children [[box
                                 :child [:div (pp/pretty-print @s/state)]]

                                [gap :size "2em"]

                                [box
                                 :child [:div (-> code t/parse pp/pretty-print)]]]]))]]))

(defonce show-code-box (atom false))

(defn root-component []
  (let [modal (s/modal!)
        error (s/error!)
        active (s/active!)
        code (s/code!)]
    [v-box
     :height "inherit"
     :children [[main-component]

                [gap :size "1"]

                (if (and @show-code-box (not= nil code))

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
