(ns jarvis.state
  (:require
   [reagent.core :refer [atom]]
   [jarvis.util :as util]))

(defrecord JarvisState [nodes active error modal])

(def empty-state
  (JarvisState. [] 0 nil nil))

(defonce state (atom empty-state))

(defn reset-state! []
  (reset! state empty-state))

(defn nodes! []
  (:nodes @state))

(defn nodes-length! []
  (count (nodes!)))

(defn active! []
  (-> (:active @state)
      (or  0)
      (min (- (nodes-length!) 1))
      (max 0)))

(defn error! []
  (:error @state))

(defn modal! []
  (:modal @state))

(defn valid-index? [index]
  (and (number? index) (< index (nodes-length!)) (< -1 index)))

(defn code!
  ([]
   (code! (active!)))
  ([index]
   (let [nodes (nodes!)]
     (if (valid-index? index)
       (nth (nodes!) index)
       nil))))

(defn- update-field [struct tuple]
  (let [field (first tuple)
        fun (last tuple)]
    (update-in struct field fun)))

(defn- update-fields [field fun & args]
  (swap! state #(reduce update-field % (conj (partition 2 args) [field fun]))))

(defn set-error [e]
  (update-fields
   [:error] (constantly e)))

(defn set-modal [e]
  (update-fields
   [:modal] (constantly e)))

(defn set-active [a]
  (when (valid-index? a))
  (update-fields
   [:active] (constantly a)))

(defn inc-active []
  (set-active (+ 1 (active!))))

(defn reset-error [] (set-error nil))

(defn reset-modal [] (set-modal nil))

(defn push-code! [code]
  (update-fields
   [:nodes] #(conj % code)))

(defn- insert-code! [code index]
  (update-fields
   [:nodes] #(assoc % index code)))

(defn push-code
  ([code]
   (push-code code nil))
  ([code index]
   (if-not (valid-index? index)
     (push-code! code)
     (insert-code! code index))))

(defn nodes-empty? []
  (< (nodes-length!) 1))

(defn pop-code []
  (let [last (- (nodes-length!) 1)]
    (if-not (nodes-empty?)
      (update-fields
       [:nodes] pop
       [:active] #(if (< % last) % last))
      (set-error (js/Error. "Can't remove from empty list!")))))
