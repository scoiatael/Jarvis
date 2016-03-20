(ns jarvis.handlers
  (:require [re-frame.core :as r-f :refer [register-handler dispatch]]
            [jarvis.syntax.core :as t]
            [jarvis.syntax.walk :as walk]
            [jarvis.syntax.parser :as parser]
            [jarvis.util.file :as file]
            [jarvis.util.nrepl :as nrepl]
            [jarvis.util.logger :as util]
            [jarvis.state.helpers :as s]))

;; -- Helpers ------------

(defn- ingest-form [form]
  (->> form
       t/parse))

(defn- check-code [code]
  (t/check #(dispatch [:add-error %1 %2]) code))

;; TODO: doseq
(defn- each [f args]
  (loop [s (seq args)]
    (when-not (empty? s)
      (f (first s))
      (recur (rest s)))))

(defn- recheck-path [path]
  (if (> (count path) 1)
    ;; We cut part of node, need to recheck
    (dispatch [:recheck (nth path 1)])))

(defn- unmark [db id]
  (if (nil? id)
    db
    (-> db
        (s/update-fields [:active] #(if (= id %) nil %))
        (s/update-node id #(walk/with-info % {:marked false})))))

(def ^:private middlewares
  [r-f/debug
   r-f/trim-v])

;; -- Event Handlers -----

(defn register! []

  (register-handler
   :initialise-db
   middlewares
   (fn [_ _]
     s/empty-state))

  (register-handler
   :add-suggestion
   middlewares
   (fn [db [ns ns-funs]]
     (s/update-suggestions db {ns ns-funs})))

  (register-handler
   :update-suggestions
   middlewares
   (fn [db _]
     (doseq [ns ["user" "clojure.core"]]
       (nrepl/functions! ns #(dispatch [:add-suggestion ns %])))
     db))

  (register-handler
   :add-error
   middlewares
   (fn [db [id err]]
     (s/update-node db id #(walk/with-err % err))))

  (register-handler
   :unmark
   middlewares
   (fn [db [id]]
     (unmark db id)))

  (register-handler
   :mark
   middlewares
   (fn [db [id]]
     (-> db
         (unmark (:active db))
         (s/update-fields [:active] (constantly id))
         (s/update-node id #(walk/with-info % {:marked true})))))

  (register-handler
   :check
   middlewares
   (fn [db _]
     (let [nodes (s/nodes db)]
       (each check-code nodes))
     db))

  (register-handler
   :push-code
   middlewares
   (fn [db [code]]
     (let [parsed (parser/form code)
           error (:error parsed)
           form (:form parsed)]
       (if (nil? error)
         (do
           (nrepl/eval! form)
           (let [new-db (s/push-code db (ingest-form form))]
             ;; TODO: check only new code
             (dispatch [:check])
             (dispatch [:update-suggestions])
             new-db))
         (s/update-fields db [:error] (constantly error))))))

  (register-handler
   :push-file
   middlewares
   (fn [_ [contents]]
     (let [parsed (->> contents parser/file (map ingest-form))]
       (let [new-db (reduce s/push-code s/empty-state parsed)]
         (dispatch [:check])
         (dispatch [:update-suggestions])
         new-db))))

  (register-handler
   :set-modal
   middlewares
   (fn [db _]
     (s/update-fields db [:modal] (constantly true))))

  (register-handler
   :add-new-node
   middlewares
   (fn [db _]
     (dispatch [:set-modal])
     db))

  (register-handler
   :pop-code
   middlewares
   (fn [db _]
     (s/pop-code db)))

  (register-handler
   :reset-error
   middlewares
   (fn [db _]
     (s/update-fields db [:error] (constantly nil))))

  (register-handler
   :reset-modal
   middlewares
   (fn [db _]
     (s/update-fields db [:modal] (constantly nil))))

  (def ^:private tmp-file "examples/file1.clj")

  (register-handler
   :open-file
   middlewares
   (fn [db [fname]]
     (let [fname tmp-file]
       (file/open fname (fn [contents]
                          (nrepl/open! fname)
                          (dispatch [:push-file contents]))))
     db))

  (register-handler
   :write-file
   middlewares
   (fn [db [fname contents]]
     (let [fname tmp-file]
       (file/write fname contents #(nrepl/open! fname)))
     db))

  (register-handler
   :toggle-pasting
   middlewares
   (fn [db args]
     (let [swap? (empty? args)
           new-value (first args)
           modify-fn (if swap? not (constantly new-value))]
       (s/update-fields db [:pasting] modify-fn))))

  (register-handler
   :recheck
   middlewares
   (fn [db [node]]
     (let [to-check node
           code-to-check (->> to-check (s/code db) t/strip)]
       (let [new-db (s/push-code db to-check (ingest-form code-to-check))]
         (dispatch [:check])
         new-db))))

  (register-handler
   :paste-node
   middlewares
   (fn [db [path node-id]]
     (let [new-db (s/paste-node db path node-id (:pasting db))]
       (dispatch [:toggle-pasting])
       (recheck-path path)
       new-db)))

  (register-handler
   :cut-node
   middlewares
   (fn [db [path node-id]]
     (let [new-db (s/remove-node db path node-id)]
       (recheck-path path)
       (dispatch [:toggle-pasting node-id])
       new-db)))

  (register-handler
   :delete
   middlewares
   (fn [db _]
     (dispatch [:toggle-pasting nil])
     db))

  (register-handler
   :undo
   middlewares
   (fn [db _] db))

  (register-handler
   :redo
   middlewares
   (fn [db _] db)))
