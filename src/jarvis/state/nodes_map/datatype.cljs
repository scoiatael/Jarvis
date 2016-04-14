(ns jarvis.state.nodes-map.datatype
  (:require [jarvis.syntax.walk :as walk]
            [schema.core :as s]))

(defprotocol Stub
  (expand [this nmap]))

(defn- nmap_node? [node]
  (if (satisfies? walk/Info node)
    (nmap_node? (walk/value node))
    (if (seq? node)
      (every? #(satisfies? Stub %) node)
      (not (seqable? node)))))

(defn schema [& roots-keys]
  {:roots (into {} (map #(vector (s/optional-key %) s/Int) roots-keys))
   :nmap {s/Int (s/pred nmap_node?)}
   :index s/Num})

(def fresh {:roots {}
            :nmap {}
            :index 0})

(defrecord StubImpl [index]
  Stub
  (expand [this nmap] (-> this :index nmap)))

(defn- with-id-info [node id]
  (if (satisfies? walk/Info node)
    (walk/with-info node {:id id})
    node))

(defn- expand-node [nmap node]
  (let [expanded (if (satisfies? Stub node)
                   (-> node (expand nmap) (with-id-info (:index node)))
                   node)]
    (walk/walk (partial expand-node nmap) identity expanded)))

(defn expand-node-index [nmap node-index]
  (expand-node (:nmap nmap) (StubImpl. node-index)))
