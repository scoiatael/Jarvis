(ns jarvis.state.nodes-map
  (:require [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]
            [schema.core :as s]))

(defprotocol Stub
  (expand [this nmap])
  (empty-stub? [this]))

(defn- nmap_node? [node] (or (satisfies? walk/Info node) (not (seq? node)) (every? #(satisfies? Stub %) node)))

(def schema {:root (s/pred #(satisfies? Stub %))
             :nmap {s/Int (s/pred nmap_node?)}
             :index s/Num})
(defrecord NodesMap [root nmap index])

(defrecord StubImpl [index]
  Stub
  (expand [this nmap] (-> this :index nmap))
  (empty-stub? [this] false))

(defn fresh []
  (NodesMap. (StubImpl. 0) {0 '()} 1))

(defn- with-id-info [node id]
  (if (satisfies? walk/Info node)
    (walk/with-info node {:id id})
    node))

(defn- expand-node [nmap node]
  (let [expanded (if (satisfies? Stub node)
                   (-> node (expand nmap) (with-id-info (:index node)))
                   node)]
    (walk/walk (partial expand-node nmap) identity expanded)))

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

(defn expand-node-index [nmap node-index] (expand-node (:nmap nmap) (StubImpl. node-index)))
(defn nodes [nmap] (expand-node (:nmap nmap) (:root nmap)))

(defn push-root [nmap form]
  (let [converted (convert nmap form)
        new-root (:root converted)
        old-root (:root nmap)]
    (-> converted
        (update-in [:root] (constantly old-root))
        (update-in [:nmap (:index old-root)] #(conj (into [] %) new-root)))))

(defn replace-node [pos node list]
  (map #(if (= (:index %) pos) node %) list))

(defn swap-at-root [nmap index form]
  (let [converted (convert nmap form)
        new-root (:root converted)
        old-root (:root nmap)]
    (-> converted
        (update-in [:root] (constantly old-root))
        (update-in [:nmap (:index old-root)] #(replace-node index new-root (into [] %))))))

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

(defn pop-root [nmap]
  (let [root-value (-> nmap :root (expand (:nmap nmap)))
        obsolete-node (last root-value)
        root (-> nmap :root :index)]
    (if (nil? obsolete-node)
      nmap
      (let [obsolete-index (:index obsolete-node)]
        (-> nmap
            (update-in [:nmap root] butlast)
            (update-in [:nmap] #(dissoc % obsolete-index))
            ;; TODO: run GC periodically, not each time
            clean-garbage)))))

(defn root-length [nmap]
  (let [root-value (-> nmap :root (expand (:nmap nmap)))]
    (count root-value)))

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
