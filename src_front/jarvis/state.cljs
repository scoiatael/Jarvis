(ns jarvis.state
  (:require
   [reagent.core :refer [atom]]
   [cljs.tools.reader.edn :as edn]))

(defrecord JarvisState [nodes active error])

(defonce state (atom (JarvisState. [{:a 1 :b 2 :c '[1 2]}] 0 nil)))

(defn active! []
  (or (:active @state) 0))

(defn nodes! []
  (:nodes @state))

(defn error! []
  (:error @state))

(defn code! []
  (nth (nodes!) (active!)))

(defn- update-field [struct tuple]
  (let [field (first tuple)
        fun (last tuple)]
    (update-in struct field fun)))

(defn- update-fields [field fun & args]
  (swap! state #(reduce update-field % (conj (partition 2 args) [field fun]))))

(defn add-empty-node []
    (update-fields
     [:nodes] #(conj % '())
     [:active] inc))

(defn set-error [e]
  (update-fields
   [:error] (constantly e)))

(defn set-active [a]
  (update-fields
   [:active] (constantly a)))

(defn reset-error [] (set-error nil))

(defn push-code [code]
  (let [active (active!)
        old-code (code!)
        edn-code (try
                   (edn/read-string code)
                   (catch js/Error e
                     (print "Error" e)
                     (set-error e)
                     old-code))]
    (update-fields
     [:nodes] #(assoc % active edn-code))))

(defn almost-empty? []
  (< (count (nodes!)) 2))

(defn pop-code []
  (let [last (- (count (nodes!)) 2)]
    (if-not (almost-empty?)
      (update-fields
       [:nodes] pop
       [:active] #(if (< % last) % last))
      (set-error (js/Error. "Can't remove last item!")))))
