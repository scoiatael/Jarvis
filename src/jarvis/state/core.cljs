(ns jarvis.state.core
  (:require [jarvis.util.core :as util]
            [jarvis.state.nodes-map.core :as nmap]
            [schema.core :as s]))

(def schema {:nodes (nmap/schema :file :snips :tmp)
             :suggestions {s/Str [s/Symbol]}
             (s/optional-key :active) (s/maybe s/Num)
             (s/optional-key :error) (s/maybe js/Error)
             (s/optional-key :modal) (s/maybe s/Bool)
             (s/optional-key :pasting) (s/maybe s/Num)
             (s/optional-key :nrepl-connection) (s/maybe s/Bool)})

(def empty-state {:nodes nmap/fresh
                  :suggestions {}})

(defn with-empty-nodes [db]
  (assoc-in db [:nodes] (:nodes empty-state)) )

(defn nodes [state]
  (-> state :nodes nmap/nodes))

(defn code [state index]
  (nmap/expand-node-index (:nodes state) index))

(def update-fields util/update-fields)

(defn push-code
  ([state code]
   (update-fields state
                  [:nodes] #(nmap/push-root % code)))
  ([state index code]
   (update-fields state
                  [:nodes] #(nmap/swap-at-root % index code))))

(defn update-node [state node-id update]
  (update-in state [:nodes] #(nmap/update-node % node-id update)))

(defn update-suggestions [state suggestions]
  (update-in state [:suggestions] #(into % suggestions)))

(defn remove-node [state path node-id]
  (update-in state [:nodes] #(nmap/remove-node % path node-id)))

(defn paste-node [state path node-id node]
  (update-in state [:nodes] #(nmap/paste-node % path node-id node)))

(defn pasting? [state] (:pasting state))
