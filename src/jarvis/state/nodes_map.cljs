(ns jarvis.state.nodes-map
  (:require [jarvis.util.logger :as util]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]))

(defprotocol Stub
  (expand [this nmap])
  (empty-stub? [this]))

(defrecord NodesMap [root nmap index])

(defrecord StubImpl [index]
  Stub
  (expand [this nmap] (-> this :index nmap))
  (empty-stub? [this] false))

(defrecord NilImpl [index]
  Stub
  (expand [this nmap] (-> this :index nmap))
  (empty-stub? [this] true))

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

(defn nodes [nmap] (expand-node (:nmap nmap) (:root nmap)))

(defn fresh []
  (NodesMap. (StubImpl. 0) {0 '()} 1))

(defn push-root [nmap form]
  (let [converted (convert nmap form)
        new-root (:root converted)
        old-root (:root nmap)]
    (-> converted
        (update-in [:root] (constantly old-root))
        (update-in [:nmap (:index old-root)] #(conj (into [] %) new-root)))))

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

(defn update-node [nmap node-id update]
  (update-in nmap [:nmap node-id] update))

(defn- map-remove [node-id struct pad]
  (let [kv-with-node-id? #(-> % :index #{node-id})]
    (reduce (fn [res tuple]
              (if (some kv-with-node-id? tuple)
                (let [removed (map #(if (kv-with-node-id? %) pad %) tuple)]
                  (if (some #(not (empty-stub? %)) removed)
                    (conj res removed)
                    res))
                (conj res tuple))) '() struct)))

(defn- is-map? [item]
  (and (satisfies? walk/Info item) (= :map (-> item walk/info :type))))

(defn- generic-remove [node-id item pad struct]
  (if (is-map? item)
    (flatten (map-remove node-id (partition 2 struct) pad))
    (filter #(not= (:index %) node-id) struct)))

(defn- get-real-node [nmap path]
  (let [inter-index (last path)
        item (-> nmap :nmap (get inter-index))
        index (if (satisfies? walk/Info item) (-> item walk/value :index) inter-index)]
    [index item]))

(def wrapped-nil (walk/wrap nil))

(defn- generate-new-nil [nmap]
  (let [this-index (:index nmap)]
    [(NilImpl. this-index)
     (-> nmap
         (update-in [:index] inc)
         (update-in [:nmap] #(assoc % this-index wrapped-nil)))]))

(defn remove-node [nmap path node-id]
  (let [[index item] (get-real-node nmap path)
        [pad updated-nmap] (generate-new-nil nmap)]
    (assert (not= nil (expand pad (:nmap updated-nmap))))
    (update-in updated-nmap [:nmap index] (partial generic-remove node-id item pad))))

(defn replace-node [pos node list]
  (map #(if (= % pos) node %) list))

(defn- seq-insert [pos node list]
  (if (map? pos)
    (let [offset (:after pos)]
      (let [[before after] (split-at offset list)]
        (concat before [node] after)))
    (replace-node pos node list)))

(defn- make-tuple [offset node pad]
  (assert (#{0 1} offset))
  (case offset
    0 [node pad]
    1 [pad node]))

(defn- map-insert [pos node list pad]
  (if (map? pos)
    (let [offset (:after pos)]
      (concat list (make-tuple offset node pad)))
    (replace-node pos node list)))

(defn- generic-insert [node-id item node pad struct]
  (if (is-map? item)
    (map-insert node-id node struct pad)
    (seq-insert node-id node struct)))

(defn paste-node [nmap path node-id node]
  (let [[index item] (get-real-node nmap path)
    [pad updated-nmap] (generate-new-nil nmap)]
    (assert (not= nil (expand pad (:nmap updated-nmap))))
    (update-in updated-nmap [:nmap index] (partial generic-insert node-id item (StubImpl. node) pad))))
