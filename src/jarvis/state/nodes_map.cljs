(ns jarvis.state.nodes-map
  (:require [jarvis.util.logger :as util]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]))

(defprotocol Stub
  (expand [this nmap]))

(defrecord NodesMap [root nmap index])

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

(defn- next-index [nmap]
  (:index (update-in nmap [:index] #(+ 1 (or % 0)))))

(defn- flatten! [index-atom map-atom form]
  (let [this-index @index-atom
        bind-this (fn [new-form]
                    (swap! map-atom #(assoc % this-index new-form))
                    (StubImpl. this-index))]
    (swap! index-atom #(+ 1 %))
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

(defn nodes [nmap] (expand-node (:nmap nmap) (:root nmap)))

(defn fresh []
  (NodesMap. (StubImpl. 0) {0 []} 1))

(defn push-root [nmap form]
  (let [converted (convert nmap form)
        new-root (:root converted)
        old-root (:root nmap)]
    (-> converted
        (update-in [:root] (constantly old-root))
        (update-in [:nmap (:index old-root)] #(conj % new-root)))))

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
            (update-in [:nmap root] pop)
            (update-in [:nmap] #(dissoc % obsolete-index))
            ;; TODO: run GC periodically, not each time
            clean-garbage)))))

(defn root-length [nmap]
  (let [root-value (-> nmap :root (expand (:nmap nmap)))]
    (count root-value)))
