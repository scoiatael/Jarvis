(ns jarvis.views.error
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.util.core :as util]
            [re-frame.core :as r-f :refer [subscribe dispatch]]))

(defn- error-panel [error]
  [rc/modal-panel :child (.-message error)
   :wrap-nicely? true
   :backdrop-color sol/red
   :backdrop-opacity 0.4
   :backdrop-on-click #(dispatch [:error-backdrop-clicked])])

(defn render []
  (let [error (subscribe [:error])]
    (fn []
      (when @error
        [error-panel @error]))))
