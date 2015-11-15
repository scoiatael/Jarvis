(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap] :as rc]
            [cljs.tools.reader.edn :as edn]
            [reagent.core :refer [atom]]
            [jarvis.types :as t]
            [jarvis.pretty-print :as pp]
            [jarvis.render :as r]))

(defonce state (atom {:code '{:a 1 :b 2 :c '[1 2]}}))

(defn push-code [code] (try
                         (swap! state #(assoc % :code (edn/read-string code)))
                         (catch js/Error e
                           (print "Error" e)
                           (swap! state #(assoc % :error e)))))

(defn reset-error [] (swap! state #(assoc % :error nil)))

(defn root-component []
  (let [code (:code @state)
        error (:error @state)]
    [v-box
     :height "inherit"
     :children [[h-box
                 :height "60%"
                 :children [[box :size "200px" :child "Nav"]
                            [box
                             :size "1"
                             :child [r/render (-> code t/parse)]]
                            [box
                             :size "1"
                             :child [:div (-> code t/parse pp/pretty-print)]]]]

                [gap :size "1"]
                [box
                 :width "inherit"
                 :child [input-textarea
                         :on-change push-code
                         :model (pp/pretty-print code)
                         :width "inherit"]]
     (if-not (nil? error)
       [rc/modal-panel :child (.-message error)
                       :wrap-nicely? true
                       :backdrop-color "#660000"
                       :backdrop-opacity 0.4
                       :backdrop-on-click reset-error])]]))

(defn main [] [root-component] )
