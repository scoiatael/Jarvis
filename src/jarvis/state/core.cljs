(ns jarvis.state.core
  (:require [jarvis.util.core :as util]
            [jarvis.state.nodes-map.core :as nmap]
            [schema.core :as s]))

(def schema {:nodes nmap/schema
             :suggestions {s/Str [s/Symbol]}
             :tab s/Keyword ;; TODO one of tabs
             (s/optional-key :server-path) s/Str
             (s/optional-key :focus) (s/maybe [(s/one s/Num "Node ID") s/Any]) ;; TODO: path instead of s/Any
             (s/optional-key :active) (s/maybe s/Num)
             (s/optional-key :error) (s/maybe js/Error)
             (s/optional-key :modal) (s/maybe s/Bool)
             (s/optional-key :pasting) (s/maybe s/Num)
             (s/optional-key :nrepl-connection) (s/maybe s/Bool)})

(def ^:constant roots #{:defs :scratch})
(def ^:constant tabs #{:edit :eval})

(def empty-state {:nodes (reduce #(assoc-in %1 [:nmap %2] '()) nmap/fresh roots)
                  :tab :edit
                  :suggestions {}})

(defn- nodes [state root]
  {:pre (roots root)}
  (let [nm (:nodes state)]
    (nmap/expand-node nm root)))

(defn defs [state]
  (nodes state :defs))

(defn scratch [state]
  (nodes state :scratch))

(defn code [state index]
  (nmap/expand-node (:nodes state) index))

(def ^:private update-fields util/update-fields)

(defn push-node [state root node]
  {:pre [(roots root)]}
  (update-in state
             [:nodes :nmap root]
             #(conj % (nmap/wrap-id node))))

(defn push-code [state root code]
  {:pre [(roots root)]}
  (update-in state
             [:nodes] (fn [nm]
                        (let [[index converted] (nmap/convert nm code)]
                          (update-in converted
                                     [:nmap root] #(conj % index))))))

(defn push-temp-code [state code]
  (let [nm (:nodes state)
        [index converted] (nmap/convert nm code)]
    [(:index index) (assoc-in state [:nodes] converted)]))

(defn inject-code [state old-index code]
  (update-in state
             [:nodes] (fn [nm]
                        (let [[index converted] (nmap/convert nm code)
                              map-index (:index index)]
                          (update-in converted
                                     [:nmap] #(clojure.set/rename-keys % {map-index old-index}))))))

(defn update-node [state node-id update]
  (update-in state [:nodes] #(nmap/update-node % node-id update)))

(defn update-suggestions [state suggestions]
  (update-in state [:suggestions] #(into % suggestions)))

(defn remove-node [state path node-id]
  (update-in state [:nodes] #(nmap/remove-node % path node-id)))

(defn paste-node [state path node-id node]
  (update-in state [:nodes] #(nmap/paste-node % path node-id node)))

(defn pasting? [state] (:pasting state))

(defn context-id [state] (or (:pasting state) (:focus state)))
(defn context [state]
  (cond
    (:pasting state) :pasting
    (:focus state) :focus
    :else nil))
