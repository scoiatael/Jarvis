(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
            [reagent.core :refer [atom]]
            [jarvis.state :as s]
            [jarvis.types :as t]
            [jarvis.pretty-print :as pp]
            [jarvis.render :as r]))

(defn- render-code [active item index]
  (let [rendered [r/render (-> item t/parse)]]
    (if (= active index)
      [rc/border
       :border "1px dashed red"
       :child rendered]
      rendered)))

(defn- render-codes [cs a]
  [h-box
   :children (map-indexed #(render-code a %2 %1) cs)])

(defn- render-error [error]
  [rc/modal-panel :child (.-message error)
   :wrap-nicely? true
   :backdrop-color "#660000"
   :backdrop-opacity 0.4
   :backdrop-on-click s/reset-error])

(defn- main-component []
  (let [codes (s/nodes!)
        active (s/active!)
        code (nth codes active)]
    [h-box
      :children [[box :size "200px" :child "Nav"]
                 [box
                  :size "1"
                  :child [render-codes codes active]]
                 [v-box
                  :children [[gap :size "1"]

                             [rc/md-circle-icon-button
                              :md-icon-name "zmdi-plus"
                              :on-click s/add-empty-node]]]
                 [v-box
                  :size "1"
                  :children [[box
                              :child [:div (-> code t/parse pp/pretty-print)]]
                             [box
                              :child [:div (pp/pretty-print @s/state)]]]]]]))

(defn root-component []
  (let [error (s/error!)
        code (s/code!)]
    [v-box
     :height "inherit"
     :children [[main-component]

                [gap :size "1"]

                [box
                 :width "inherit"
                 :child [input-textarea
                         :on-change s/push-code
                         :model (pp/pretty-print code)
                         :width "inherit"]]
     (if-not (nil? error)
       (render-error error))]]))

(defn main [] [root-component] )
