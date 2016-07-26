(ns jarvis.state.nodes-map.garbage-collector
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.state.nodes-map.datatype :refer [expand Stub StubImpl]]))

(defn- all-indices! [nmap list-atom node]
  (when (satisfies? Stub node)
    (swap! list-atom #(conj % node))
    (walk/walk (partial all-indices! nmap list-atom) identity (expand node (:nmap nmap)))))

(defn- all-indices [nmap]
  (let [list-atom (atom #{})
        nodes (->> nmap :nodes (filter #(keyword? (first %))) (map second))]
    (map
     #(all-indices! nmap list-atom (second %))
     nodes)
    (into #{} (map :index (flatten @list-atom)))))

(defn clean-garbage [nmap]
  (let [valid-indices(all-indices nmap)
        valid-index? #(or (contains? valid-indices %) (keyword? %))]
    (-> nmap
        (update-in [:nmap] (fn [nmap] (->> nmap
                                          (filter #(valid-index? (first %)))
                                          (into {})))))))
