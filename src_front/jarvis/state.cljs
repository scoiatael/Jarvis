(ns jarvis.state
  (:require
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

(defn push-code [code]
  (let [
        active (active!)
        old-code (nth (:nodes @state) active)
        edn-code (try
                   (edn/read-string code)
                   (catch js/Error e
                     (print "Error" e)
                     (swap! state #(assoc % :error e))
                     old-code))]
    (update-fields
     [:nodes] #(assoc % active edn-code))))

(defn add-empty-node []
    (update-fields
     [:nodes] #(conj % '())
     [:active] inc))

(defn reset-error [] (swap! state #(assoc % :error nil)))
