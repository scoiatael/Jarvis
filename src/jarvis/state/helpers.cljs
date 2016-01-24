(ns jarvis.state.helpers
  (:require [jarvis.util.logger :as util]
            [jarvis.state.nodes-map :as nmap]))

(defrecord JarvisState [nodes active error modal])

(def empty-state (JarvisState. (nmap/fresh) 0 nil nil))

(defn nodes [state]
  (-> state :nodes nmap/nodes))

(defn nodes-length [state]
  (-> state :nodes nmap/root-length))

(defn error [state]
  (:error state))

(defn modal [state]
  (:modal state))

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

(defn- update-fields [state field fun & args]
  (reduce update-field state (conj (partition 2 args) [field fun])))

(defn set-error [state e]
  (update-fields state
   [:error] (constantly e)))

(defn reset-error [state] (set-error state nil))

(defn set-modal [state e]
  (update-fields state
   [:modal] (constantly e)))

(defn reset-modal [state] (set-modal state nil))

(defn push-code [state code]
  (update-fields state
   [:nodes] #(nmap/push-root % code)))

(defn nodes-empty? [state]
  (< (nodes-length state) 1))

(defn pop-code [state]
  (update-in state [:nodes] nmap/pop-root))
