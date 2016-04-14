(ns jarvis.state.nodes-map.conversion
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.state.nodes-map.datatype :refer [expand Stub StubImpl]]))

(defn- next-index [nmap]
  (:index (update-in nmap [:index] #(+ 1 (or % 0)))))

(defn- flatten! [index-atom map-atom form]
  (let [this-index @index-atom
        bind-this (fn [new-form]
                    (swap! map-atom #(assoc % this-index new-form))
                    (StubImpl. this-index))]
    (swap! index-atom inc)
    (walk/walk (partial flatten! index-atom map-atom) bind-this form)
    (StubImpl. this-index)))

(defn- convert [nmap form]
  (let [first-index (:index nmap)
        index-atom (atom first-index)
        map-atom (atom (:nmap nmap))
        flat-form (flatten! index-atom map-atom form)]
    (-> nmap
        (update-in [:root] (constantly (StubImpl. first-index)))
        (update-in [:index] (constantly @index-atom))
        (update-in [:nmap] (constantly @map-atom)))))
