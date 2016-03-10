(ns jarvis.state.core
  (:require [reagent.core :as reagent]
            [jarvis.util.logger :as util]
            [jarvis.state.helpers :as h]))

(defonce ^:private state (reagent/atom h/empty-state))

(defn- modify [field fun] (swap! state #(h/update-fields % [field] fun)))

;; Impure
(defn reset-state! [] (reset! state h/empty-state))
(defn push-code! [code] (swap! state #(h/push-code % code)))
(defn insert-code-at [path code] (swap! state #(h/push-code % path code)))
(defn set-error! [e] (modify :error (constantly e)))
(defn reset-error! [] (set-error! nil))

(defn set-modal! [e] (modify :modal (constantly e)))
(defn reset-modal! [] (set-modal! nil))

(defn swap-active! [f] (modify :active f))

(defn swap-pasting! [f] (modify :pasting f))

(defn pop-code! [] (swap! state #(h/pop-code %)))
(defn update-node [node-id update] (swap! state #(h/update-node % node-id update)))
(defn update-suggestions [suggestions] (swap! state #(h/update-suggestions % suggestions)))
(defn remove-node [path node-id] (swap! state #(h/remove-node % path node-id)))
(defn paste-node [path node-id node] (swap! state #(h/paste-node % path node-id node)))

;; Pure
(defn fetch [] @state)
(defn nodes-length [] (h/nodes-length (fetch)))
(defn code [& args] (apply h/code (into [(fetch)] args)))
(defn nodes [] (h/nodes (fetch)))
(defn active [] (:active (fetch)))
(defn pasting [] (:pasting (fetch)))
(defn pasting? [] (h/pasting? (fetch)))
