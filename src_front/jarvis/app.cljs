(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
            [reagent.core :refer [atom]]
            [jarvis.state :as s]
            [jarvis.types :as t]
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
       :border (str "1px dashed " (if (= active index) "red" "transparent"))
       :child rendered]))

(defn- render-codes [cs a]
  [v-box
   :children (map-indexed #(render-code a %2 %1) cs)])

(defn- render-error [error]
  [rc/modal-panel :child (.-message error)
   :wrap-nicely? true
   :backdrop-color "#660000"
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
        active (s/active!)
        code (nth codes active)]
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
                 [v-box
                  :size "1"
                  :children [[box
                              :child [:div (-> code t/parse pp/pretty-print)]]
                             [box
                              :child [:div (pp/pretty-print @s/state)]]]]]]))

(defn root-component []
  (let [modal (s/modal!)
        error (s/error!)
        active (s/active!)
        code (s/code!)]
    [v-box
     :height "inherit"
     :children [[main-component]

                [gap :size "1"]

                [box
                 :width "inherit"
                 :child (edit-elem code active)]

                (if-not (nil? error)
                  (render-error error)

                  (if-not (nil? modal)
                    (render-modal modal)))]]))

(defn main [] [root-component] )
