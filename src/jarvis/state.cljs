(ns jarvis.state
  (:require
   [reagent.core :refer [atom]]
   [cljs.tools.reader.edn :as edn]
   [cljs.tools.reader.reader-types :as types]
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
      (max 0)
      (min (- (nodes-length!) 1))))

(defn error! []
  (:error @state))

(defn modal! []
  (:modal @state))

(defn code! []
  (let [nodes (nodes!)
        active (active!)
        nodes-length (nodes-length!)]
    (if (< 0 nodes-length)
      (nth (nodes!) (active!))
      nil)))

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

(defn- push-parsed-code [code index]
  (let [index (active!)]
    (update-fields
     [:nodes] #(assoc % index code))))

(defn push-code [code index]
  (let [old-code (code!)
        edn-code (try
                   (edn/read-string code)
                   (catch js/Error e
                     (print "Error" e)
                     (set-error e)
                     old-code))]
    (push-parsed-code edn-code index)))

(defn almost-empty? []
  (< (count (nodes!)) 2))

(defn pop-code []
  (let [last (- (count (nodes!)) 2)]
    (if-not (almost-empty?)
      (update-fields
       [:nodes] pop
       [:active] #(if (< % last) % last))
      (set-error (js/Error. "Can't remove last item!")))))

(defn push-file-part [reader]
  ;; TODO make this a precondition
  ;; reader should be types/Reader
  (try
    (add-empty-node)
    (let [active (active!)]
      (push-parsed-code (edn/read {} reader) active))
    (inc-active)
    (push-file-part reader)
    (catch js/Error e
      (when (not= "EOF" (.-message e))
        (util/error! "Error while parsing file!" e)))))

(defn push-file [contents]
  (reset-state!)
  (let [reader (types/string-push-back-reader contents)]
    (push-file-part reader)))
