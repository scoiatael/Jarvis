(ns jarvis.state.nodes-map.datatype
  (:require [jarvis.syntax.walk :as walk]
            [schema.core :as s]))

(defprotocol Stub
  (expand [this nmap]))

(defn- nmap_node? [node]
  (if (satisfies? walk/Info node)
    (nmap_node? (walk/value node))
    (or (satisfies? Stub node)
        (if (seq? node)
          (every? #(satisfies? Stub %) node)
          (not (seqable? node))))))

(def schema
  {:nmap {(s/conditional
           keyword? s/Keyword
           :else s/Int) (s/pred nmap_node?)}
   :index s/Num})

(def fresh
  {:nmap {}
   :index 0})

(defrecord StubImpl [index]
  Stub
  (expand [this nmap] (get nmap (:index this))))

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
