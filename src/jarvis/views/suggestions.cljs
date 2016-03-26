(ns jarvis.views.suggestions
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [jarvis.util.core :as util]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- namespace-box [name functions]
  (let [on-click #(dispatch [:namespace-function-clicked name %])
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

(defn- suggestions-bar [suggestions]
  [v-box
   :children (->> suggestions
                  (sort-by first)
                  (map (fn [item] [namespace-box (first item) (last item)])))
   :style {:width "100%"}])

(defn render []
  (let [suggestions (subscribe [:suggestions])]
    (fn []
      [suggestions-bar @suggestions])))
