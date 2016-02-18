(ns jarvis.state.core
  (:require [reagent.core :as reagent]
            [jarvis.util.logger :as util]
            [jarvis.state.helpers :as h]))

(defonce ^:private state (reagent/atom h/empty-state))

;; Impure
(defn reset-state! [] (reset! state h/empty-state))
(defn push-code! [code] (swap! state #(h/push-code % code)))
(defn set-error! [error] (swap! state #(h/set-error % error)))
(defn set-modal! [modal] (swap! state #(h/set-modal % modal)))
(defn swap-active! [f] (swap! state #(h/swap-active % f)))
(defn reset-error! [] (swap! state #(h/reset-error %)))
(defn reset-modal! [] (swap! state #(h/reset-modal %)))
(defn pop-code! [] (swap! state #(h/pop-code %)))
(defn update-node [node-id update] (swap! state #(h/update-node % node-id update)))
(defn update-suggestions [suggestions] (swap! state #(h/update-suggestions % suggestions)))
(defn remove-node [path node-id] (swap! state #(h/remove-node % path node-id)))

;; Pure
(defn nodes-length [] (h/nodes-length @state))
(defn code [& args] (apply h/code (into [@state] args)))
(defn nodes [] (h/nodes @state))
(defn active [] (h/active @state))
(defn fetch [] @state)
