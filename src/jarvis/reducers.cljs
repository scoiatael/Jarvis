(ns jarvis.reducers
  (:require [re-frame.core :as r-f :refer [dispatch]]
            [jarvis.syntax.core :as t]
            [jarvis.syntax.walk :as walk]
            [jarvis.syntax.parser :as parser]
            [jarvis.util.file :as file]
            [jarvis.util.nrepl :as nrepl]
            [jarvis.util.ipc :as ipc]
            [jarvis.util.logger :as util]
            [jarvis.util.promise :as p]
            [jarvis.state.core :as s]))


;; -- Helpers ------------
(defn set-pasting [db value]
  (s/update-fields db [:pasting] (constantly value)))

(defn- ingest-form [form]
  (->> form
       t/parse))

(defn- check-code [code]
  (t/check #(dispatch [:add-error %1 %2]) code))

(defn- start-update-suggestions-for [ns]
  (nrepl/functions! ns #(dispatch [:add-suggestion ns %])) )

(defn- start-update-suggestions [db]
  (doseq [ns ["user" "clojure.core"]]
    (start-update-suggestions-for ns)))

(defn- start-update-user-suggestions! []
  (start-update-suggestions-for "user"))

(defn- set-error [db err]
  (s/update-fields db [:error] (constantly err)))

(defn- push-code [db code]
  (let [parsed (parser/form code)
        error (:error parsed)
        form (:form parsed)]
    (if (nil? error)
      (s/push-code db :scratch (ingest-form form))
      (set-error db error))))

(defn- set-modal [db value]
  (s/update-fields db [:modal] (constantly value)))

(defn- clear-node [db node]
  (let [to-check node
        code-to-check (->> to-check (s/code db) t/strip)]
    (let [new-db (s/inject-code db to-check (ingest-form code-to-check))]
      new-db)))

(defn- add-eval-info [code info]
  (let [id (-> code walk/info :id)]
    (dispatch [:add-eval-info id info])))

(defn- eval-node [db node]
  (let [code (s/code db node)
        stripped (t/strip code)]
    (p/then (nrepl/eval! stripped)
            #(add-eval-info code %)
            #(check-code code)))
  db)

(defn- unmark [db id field]
  (if (nil? id)
    db
    (-> db
        (s/update-node id #(walk/with-info % {field false})))))

;; -- public -------------
(defn switch-to-tab [db [tab]]
  (assoc-in db [:tab] tab))

(defn clone-node [db [node]]
  (let [code (->> node (s/code db) t/strip)]
    (s/push-temp-code db (ingest-form code))))

(defn paste-node [db [node-id path]]
  (-> (s/paste-node db path node-id (:pasting db))
      (set-pasting nil)))

(defn cut-node [db [node-id path]]
  (-> (s/remove-node db path node-id)
      (set-pasting node-id)))

(defn mark-focused [db [id path]]
  (-> db
      (unmark (-> db :focus first) :focused)
      (s/update-fields [:focus] (constantly [id path]))
      (s/update-node id #(walk/with-info % {:focused true}))))

(defn unmark-focused [db]
  (-> db
      (unmark (-> db :focus first) :focused)
      (s/update-fields [:focus] (constantly nil))))

(defn unmark-node [db [id]]
  (-> db
      (s/update-fields [:active] (constantly nil))
      (unmark id :marked)))

(defn mark-node [db [id]]
  (-> db
      (unmark (:active db) :marked)
      (s/update-fields [:active] (constantly id))
      (s/update-node id #(walk/with-info % {:marked true}))))

(defn push-namespaced-fn [db [ns fn]]
  (-> db
      (assoc-in [:modal] nil)
      (push-code (str ns "/" fn))
      (switch-to-tab [:edit])))

(def ^:private tmp-file "examples/file1.clj")

(defn open-file [db _]
  (file/open-file-dialog (fn [fname_arr]
                           (if-let [fname (first fname_arr)]
                             ;; TODO: what if not exists?
                             (file/open fname (fn [contents]
                                                (ipc/restart-server! (file/dirname fname))
                                                (dispatch [:push-file contents]))))))
  db)

(defn save-file [db _]
  (file/save-file-dialog (fn [fname]
                           (when fname
                             (let [code (->>
                                         (concat 
                                          (-> db s/defs t/strip)
                                          (-> db s/scratch t/strip))
                                         (clojure.string/join "\n"))]
                               (file/write fname code (fn []
                                                        ;; TODO: dialog? message box?
                                                        (util/log! "File saved" {:filename fname})))))))
  db)

(defn push-file [db [contents]]
    (let [parsed (->> contents parser/file (map ingest-form))]
      ;; TODO: Push to defs?
      (let [new-db (reduce #(s/push-code %1 :scratch %2) s/empty-state parsed)]
        ;; TODO: update only suggestions for user namespace
        (start-update-suggestions new-db)
        new-db)))

(defn set-node-error [db [id err]]
  (s/update-node db id #(walk/with-err % err))) 

(defn set-node-eval-info [db [id info]]
  (start-update-user-suggestions!)
  (s/update-node db id #(walk/with-info % {:eval info}))) 

(defn add-namespace-functions [db [ns ns-funs]]
  (s/update-suggestions db {ns ns-funs}))

(defn modal->code [db [code]]
  (let [new-db (push-code db code)]
    (if (nil? (:error new-db))
      (-> new-db
          (set-modal nil)
          (switch-to-tab [:edit]))
      new-db)))

(defn node-push-scratch [db [node path]]
  (-> db
      (s/remove-node path node)
      (clear-node node)
      (s/push-node :scratch node)))

(defn node-push-pasting-scratch [db _]
  (let [node (:pasting db)]
    (-> db
        (s/push-node :scratch node)
        (clear-node node)
        (set-pasting nil))))

(defn initialise-db [_ _]
  s/empty-state)

(defn update-suggestions [db _]
  (start-update-suggestions db)
  db)

(defn eval-pasting [db]
  (let [node (:pasting db)]
    (-> db
        (s/push-node :defs node)
        (clear-node node)
        (eval-node node)
        (set-pasting nil))))

(defn eval-focus [db]
  (let [node (:focus db)]
    (-> db
        (cut-node node)
        eval-pasting
        unmark-focused)))
