(ns jarvis.state.core
  (:require [reagent.core :as reagent]
            [jarvis.util.logger :as util]
            [jarvis.state.helpers :as h]))

(defonce ^:private state (reagent/atom h/empty-state))

;; Impure
(defn reset-state! [] (reset! state h/empty-state))
(defn push-code! [& args] (swap! state #(apply h/push-code (into [%] args))))
(defn set-error! [error] (swap! state #(h/set-error % error)))
(defn set-modal! [modal] (swap! state #(h/set-modal % modal)))
(defn reset-error! [] (swap! state #(h/reset-error %)))
(defn reset-modal! [] (swap! state #(h/reset-modal %)))
(defn pop-code! [] (swap! state #(h/pop-code %)))
(defn update-node [node-id update]
  (swap! state #(h/update-node % node-id update)))

;; Pure
(defn nodes-length [] (h/nodes-length @state))
(defn code [& args] (apply h/code (into [@state] args)))
(defn nodes [] (h/nodes @state))

(defn fetch [] @state)
