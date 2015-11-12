(ns jarvis.app
  (:require [re-com.core :refer [v-box box h-box input-textarea gap]]
            [cljs.tools.reader.edn :as edn]
            [cljs.pprint]
            [clojure.walk :as walk]
            [reagent.core :refer [atom]]))

(defonce state (atom {:code "some-code"}))

(defn pretty-print [struct] (with-out-str (cljs.pprint/pprint struct)))

(defn code-box [code] [:div {:style {:border "solid 1px blue"}} (pretty-print code)])

(defn code_tree [code] (apply conj [:div](walk/walk code-box identity code)))

(defn push-code [code] (swap! state #(assoc % :code (edn/read-string code))))

(defn root-component []
  (let [code (:code @state)]
    [v-box
     :height "inherit"
     :children [[h-box
                 :height "60%"
                 :children [[box :size "200px" :child "Nav"]
                            [box
                             :size "1"
                             :child [code_tree code]]]]

                [gap :size "1"]
                [box
                 :width "inherit"
                 :child [input-textarea
                         :on-change push-code
                         :model (pretty-print code)
                         :width "inherit"]]]]))

(defn main [] [root-component] )
