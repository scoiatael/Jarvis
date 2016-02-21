(ns jarvis.state.helpers
  (:require [jarvis.util.logger :as util]
            [jarvis.state.nodes-map :as nmap]))

(defrecord JarvisState [nodes active error modal suggestions pasting])

(def empty-state (JarvisState. (nmap/fresh) nil nil nil {} nil))

(defn nodes [state]
  (-> state :nodes nmap/nodes))

(defn nodes-length [state]
  (-> state :nodes nmap/root-length))

(defn error [state]
  (:error state))

(defn modal [state]
  (:modal state))

(defn suggestions [state]
  (:suggestions state))

(defn valid-index? [state index]
  (and (number? index) (< index (nodes-length state)) (< -1 index)))

(defn code
  ([state index]
   (let [nodes (nodes state)]
     (if (valid-index? state index)
       (nth nodes index)
       nil))))

(defn- update-field [struct tuple]
  (let [field (first tuple)
        fun (last tuple)]
    (update-in struct field fun)))

(defn update-fields [state field fun & args]
  (reduce update-field state (conj (partition 2 args) [field fun])))

(defn push-code [state code]
  (update-fields state
   [:nodes] #(nmap/push-root % code)))

(defn nodes-empty? [state]
  (< (nodes-length state) 1))

(defn pop-code [state]
  (update-in state [:nodes] nmap/pop-root))

(defn update-node [state node-id update]
  (update-in state [:nodes] #(nmap/update-node % node-id update)))

(defn update-suggestions [state suggestions]
  (update-in state [:suggestions] (constantly suggestions)))

(defn remove-node [state path node-id]
  (update-in state [:nodes] #(nmap/remove-node % path node-id)))

(defn paste-node [state path node-id node]
  (update-in state [:nodes] #(nmap/paste-node % path node-id node)))

(defn pasting? [state] (:pasting state))
