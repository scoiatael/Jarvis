(ns jarvis.state
  (:require
   [reagent.core :refer [atom]]
   [jarvis.util :as util]
   [jarvis.syntax.parser :as parser]))

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

(defn code!
  ([]
   (code! (active!)))
  ([index]
   (let [nodes (nodes!)
         nodes-length (nodes-length!)]
     (if (< index nodes-length)
       (nth (nodes!) index)
       nil))))

(defn- update-field [struct tuple]
  (let [field (first tuple)
        fun (last tuple)]
    (update-in struct field fun)))

(defn- update-fields [field fun & args]
  (swap! state #(reduce update-field % (conj (partition 2 args) [field fun]))))

(defn add-empty-node []
    (update-fields
     [:nodes] #(conj % '())))

(defn set-error [e]
  (update-fields
   [:error] (constantly e)))

(defn set-modal [e]
  (update-fields
   [:modal] (constantly e)))

(defn set-active [a]
  (update-fields
   [:active] (constantly a)))

(defn inc-active []
  (set-active (+ 1 (active!))))

(defn reset-error [] (set-error nil))

(defn reset-modal [] (set-modal nil))

(defn- push-parsed-code
  ([code]
   (add-empty-node)
   (push-parsed-code code (- (nodes-length!) 1)))
  ([code index]
   (update-fields
    [:nodes] #(assoc % index code))))

(defn push-code
  ([code index]
   (let [parsed (parser/form code)
         error (:error parsed)
         form (:form parsed)]
     (if (nil? error)
       (push-parsed-code form index)
       (set-error error)))))

(defn nodes-empty? []
  (< (nodes-length!) 1))

(defn pop-code []
  (let [last (- (nodes-length!) 1)]
    (if-not (nodes-empty?)
      (update-fields
       [:nodes] pop
       [:active] #(if (< % last) % last))
      (set-error (js/Error. "Can't remove from empty list!")))))

(defn push-file [contents]
  (reset-state!)
  (let [parsed (parser/file contents)]
    (map push-parsed-code parsed)))
