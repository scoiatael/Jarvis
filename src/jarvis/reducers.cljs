(ns jarvis.reducers
  (:require [re-frame.core :as r-f :refer [dispatch]]
            [jarvis.syntax.core :as t]
            [jarvis.syntax.walk :as walk]
            [jarvis.syntax.parser :as parser]
            [jarvis.util.file :as file]
            [jarvis.util.nrepl :as nrepl]
            [jarvis.util.logger :as util]
            [jarvis.state.core :as s]))


;; -- Helpers ------------
(defn- set-pasting [db value]
  (s/update-fields db [:pasting] (constantly value)))

(defn- ingest-form [form]
  (->> form
       t/parse))

(defn- check-code [code]
  (t/check #(dispatch [:add-error %1 %2]) code))

(defn- check [db]
  (doseq [node (s/nodes db)]
    (check-code node)))

(defn- start-update-suggestions [db]
  (doseq [ns ["user" "clojure.core"]]
    (nrepl/functions! ns #(dispatch [:add-suggestion ns %]))))

(defn- set-error [db err]
  (s/update-fields db [:error] (constantly err)))

(defn- push-code [db code]
  (let [parsed (parser/form code)
        error (:error parsed)
        form (:form parsed)]
    (if (nil? error)
      (do
        ;; TODO: ensure eval finished before check? show error?
        (nrepl/eval! form)
        (let [new-db (s/push-code db (ingest-form form))]
          ;; TODO: check only new code
          (check new-db)
          ;; TODO: update only suggestions for user namspace
          (start-update-suggestions new-db)
          new-db))
      (set-error db error))))

(defn- set-modal [db value]
  (s/update-fields db [:modal] (constantly value)))

(defn- recheck [db node]
  (let [to-check node
        code-to-check (->> to-check (s/code db) t/strip)]
    (let [new-db (s/push-code db to-check (ingest-form code-to-check))]
      (check new-db)
      new-db)))

(defn- recheck-path [db path]
  (if (> (count path) 1)
    ;; We cut part of node, need to recheck
    (recheck db (nth path 1))
    db))

(defn- paste-node [db node-id path]
  (-> (s/paste-node db path node-id (:pasting db))
      (recheck-path path)
      (set-pasting nil)))

(defn- cut-node [db node-id path]
  (-> (s/remove-node db path node-id)
      (recheck-path path)
      (set-pasting node-id)))

(defn- unmark [db id]
  (if (nil? id)
    db
    (-> db
        (s/update-fields [:active] #(if (= id %) nil %))
        (s/update-node id #(walk/with-info % {:marked false})))))

;; -- public -------------
(defn reset-error [db _]
  (set-error db nil))

(defn reset-modal [db _]
  (set-modal db nil))

(defn unmark-node [db [id]]
  (unmark db id))

(defn mark-node [db [id]]
  (-> db
      (unmark (:active db))
      (s/update-fields [:active] (constantly id))
      (s/update-node id #(walk/with-info % {:marked true}))))

(defn show-modal [db _]
   (s/update-fields db [:modal] (constantly true)))

(defn push-namespaced-fn [db [ns fn]]
  (push-code db (str ns "/" fn)))

(defn pop-code [db _]
  (s/pop-code db))

(def ^:private tmp-file "examples/file1.clj")

(defn open-file [db [fname]]
  (let [fname tmp-file]
    (file/open fname (fn [contents]
                       ;; TODO: ensure eval finished before check? show error?
                       (nrepl/open! fname)
                       (dispatch [:push-file contents]))))
  db)

;; (defn write-file [db [fname contents]]
;;   (let [fname tmp-file]
;;     (file/write fname contents #(nrepl/open! fname)))
;;   db)

(defn push-file [_ [contents]]
    (let [parsed (->> contents parser/file (map ingest-form))]
      (let [new-db (reduce s/push-code s/empty-state parsed)]
        (check new-db)
        ;; TODO: update only suggestions for user namspace
        (start-update-suggestions new-db)
        new-db)))

(defn set-node-error [db [id err]]
  (s/update-node db id #(walk/with-err % err))) 

(defn add-namespace-functions [db [ns ns-funs]]
  (s/update-suggestions db {ns ns-funs}))

(defn leave-pasting-mode [db _]
  (set-pasting db nil))

(defn enter-pasting-mode [db [value]]
  (set-pasting db value))

(defn modal->code [db [code]]
  (let [new-db (push-code db code)]
    (if (nil? (s/error new-db))
      (set-modal new-db nil)
      new-db)))

(defn node-paste-or-cut [db [node path]]
  (if (s/pasting? db)
    (paste-node db node path)
    (cut-node db node path)))

(defn initialise-db [_ _]
  s/empty-state)

(defn update-suggestions [db _]
  (start-update-suggestions db)
  db)
