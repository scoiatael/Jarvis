(ns jarvis.state.helpers
  (:require [jarvis.util.logger :as util]))

(defrecord JarvisState [nodes active error modal])

(def empty-state (JarvisState. [] 0 nil nil))

(defn nodes [state]
  (:nodes state))

(defn nodes-length [state]
  (count (nodes state)))

(defn active [state]
  (-> (:active state)
      (or  0)
      (min (- (nodes-length state) 1))
      (max 0)))

(defn error [state]
  (:error state))

(defn modal [state]
  (:modal state))

(defn valid-index? [state index]
  (and (number? index) (< index (nodes-length state)) (< -1 index)))

(defn code
  ([state]
   (code state (active state)))
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

(defn set-modal [state e]
  (update-fields state
   [:modal] (constantly e)))

(defn set-active [state a]
  (when (valid-index? state a))
  (update-fields state
   [:active] (constantly a)))

(defn inc-active [state]
  (set-active state (+ 1 (active state))))

(defn reset-error [state] (set-error state nil))

(defn reset-modal [state] (set-modal state nil))

(defn- push-code! [state code]
  (update-fields state
   [:nodes] #(conj % code)))

(defn- insert-code [state code index]
  (update-fields state
   [:nodes] #(assoc % index code)))

(defn push-code
  ([state code index]
   (if-not (valid-index? state index)
     (push-code! state code)
     (insert-code state code index))))

(defn nodes-empty? [state]
  (< (nodes-length state) 1))

(defn pop-code [state]
  (let [last (- (nodes-length state) 1)]
    (if-not (nodes-empty? state)
      (update-fields state
       [:nodes] pop
       [:active] #(if (< % last) % last))
      (set-error state (js/Error. "Can't remove from empty list!")))))
