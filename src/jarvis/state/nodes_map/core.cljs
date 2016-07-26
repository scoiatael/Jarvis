(ns jarvis.state.nodes-map.core
  (:require [jarvis.state.nodes-map.datatype :as dt]
            [jarvis.state.nodes-map.conversion :as cv]
            [jarvis.state.nodes-map.manipulation :as mp]))

(def schema dt/schema)
(def fresh dt/fresh)

(def expand-node dt/expand-node-index)

(def insert-as cv/insert-as)
(def convert cv/convert)
(def wrap-id cv/wrap-id)

(def update-node mp/update-node)
(def remove-node mp/remove-node)
(def paste-node mp/paste-node)

;; (defn pop-root [nmap]
;;   (let [root-value (-> nmap :root (expand (:nmap nmap)))
;;         obsolete-node (last root-value)
;;         root (-> nmap :root :index)]
;;     (if (nil? obsolete-node)
;;       nmap
;;       (let [obsolete-index (:index obsolete-node)]
;;         (-> nmap
;;             (update-in [:nmap root] butlast)
;;             (update-in [:nmap] #(dissoc % obsolete-index))
;;             ;; TODO: run GC periodically, not each time
;;             clean-garbage)))))


;; (defn root-length [nmap]
;;   (let [root-value (-> nmap :root (expand (:nmap nmap)))]
;;     (count root-value)))
