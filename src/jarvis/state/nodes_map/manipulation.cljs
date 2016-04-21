(ns jarvis.state.nodes-map.manipulation
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.state.nodes-map.datatype :refer [expand Stub StubImpl]]))

(defn- replace-node [pos node list]
  (map #(if (= (:index %) pos) node %) list))

(defn update-node [nmap node-id update]
  (update-in nmap [:nmap node-id] update))

(defn- generic-remove [node-id item struct]
  (filter #(not= (:index %) node-id) struct))

(defn- get-real-node [nmap path]
  (let [inter-index (last path)
        item (-> nmap :nmap (get inter-index))
        index (if (satisfies? walk/Info item) (-> item walk/value :index) inter-index)]
    [index item]))

(defn remove-node [nmap path node-id]
  (let [[index item] (get-real-node nmap path)]
    (update-in nmap [:nmap index] (partial generic-remove node-id item))))

(defn- seq-insert [pos node list]
  (if (map? pos)
    (let [offset (:after pos)]
      (let [[before after] (split-at offset list)]
        (concat before [node] after)))
    (replace-node pos node list)))

(defn- generic-insert [node-id item node struct]
  (seq-insert node-id node struct))

(defn paste-node [nmap path node-id node]
  (let [[index item] (get-real-node nmap path)]
    (update-in nmap [:nmap index] (partial generic-insert node-id item (StubImpl. node)))))
