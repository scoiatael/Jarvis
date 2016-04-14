(ns jarvis.state.nodes-map.garbage-collector
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.state.nodes-map.datatype :refer [expand Stub StubImpl]]))

(defn- all-indexes! [nmap list-atom node]
  (when (satisfies? Stub node)
    (swap! list-atom #(conj % node))
    (walk/walk (partial all-indexes! nmap list-atom) identity (expand node (:nmap nmap)))))

(defn all-indexes [nmap]
  (let [list-atom (atom #{})
        node (:root nmap)]
    (all-indexes! nmap list-atom node)
    (into #{} (map :index @list-atom))))

(defn clean-garbage [nmap]
  (let [valid-indexes (all-indexes nmap)]
    (-> nmap
        (update-in [:nmap] (fn [nmap] (into {} (filter #(contains? valid-indexes (first %)) nmap)))))))
