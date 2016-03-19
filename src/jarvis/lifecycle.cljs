(ns jarvis.lifecycle
  (:require [jarvis.util.nrepl :as nrepl]
            [jarvis.util.file :as file]
            [jarvis.state.core :as state]
            [jarvis.syntax.parser :as parser]
            [jarvis.syntax.core :as t]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.logger :as util]))

(def ^:const ^:private global-ns "clojure.core")

(defn- add-suggestion [ns ns-funs]
  (state/off-the-record
   (fn []
     (state/update-suggestions {ns ns-funs}))))

(defn update-suggestions []
  (doseq [ns ["user" "clojure.core"]]
    (nrepl/functions! ns (partial add-suggestion ns))))

(defn- ingest-form [form]
  (->> form
       t/parse))

(defn- add-error [id err]
  (state/off-the-record
   (fn []
     (state/update-node id #(walk/with-err % err)))))

(defn- check-code [code]
  (t/check add-error code))

(defn unmark [id]
  (state/off-the-record
   (fn []
     (when-not (nil? id)
       (state/swap-active! #(if (= id %) nil %))
       (state/update-node id #(walk/with-info % {:marked false}))))))

(defn mark [id]
  (state/off-the-record
   (fn []
     (unmark (state/active))
     (state/swap-active! (constantly id))
     (state/update-node id #(walk/with-info % {:marked true})))))

(defn each [f args]
  (loop [s (seq args)]
    (when-not (empty? s)
      (f (first s))
      (recur (rest s)))))

(defn check []
  (let [nodes (state/nodes)]
    (each check-code nodes)))

(defn push-code [code]
  (let [parsed (parser/form code)
        error (:error parsed)
        form (:form parsed)]
    (if (nil? error)
      (do
        (nrepl/eval! form)
        (state/push-code! (ingest-form form))
        ;; TODO: check only new code
        (check)
        (update-suggestions))
      (state/set-error! error))))

(defn push-file [contents]
  (state/reset-state!)
  (let [parsed (->> contents parser/file (map ingest-form))]
    (each state/push-code! parsed)
    (check)
    (update-suggestions)))

(defn set-modal [] (state/set-modal! true))
(defn add-new-node []
  (set-modal))

(defn pop-code [] (state/pop-code!))

(defn reset-error [] (state/reset-error!))
(defn reset-modal [] (state/reset-modal!))

(def ^:private tmp-file "examples/file1.clj")

(defn open-file [fname]
  (util/log! "Got open file" fname)
  (let [fname tmp-file]
    (file/open fname (fn [contents]
                       (util/log! "File contents: " contents)
                       (nrepl/open! fname)
                       (push-file contents)))))

(defn write-file [fname contents]
  (util/log! "Got write file" fname contents)
  (let [fname tmp-file]
    (file/write fname contents #(nrepl/open! fname))))

(defn- toggle-pasting [& args]
  (let [swap? (empty? args)
        new-value (first args)
        modify-fn (if swap? not (constantly new-value))]
    (state/swap-pasting! modify-fn)))

(defn- recheck [node]
  (let [to-check node
        code-to-check (-> to-check state/code t/strip)]
    ;; (util/log! "Need to check" to-check code-to-check)
    (state/insert-code-at to-check (ingest-form code-to-check))
    (check)))

(defn- recheck-path [path]
  (if (> (count path) 1)
    ;; We cut part of node, need to recheck
    (recheck (nth path 1))))

(defn paste-node [path node-id]
  (state/with-single-record
    (fn []
      (state/paste-node path node-id (state/pasting))
      (toggle-pasting)
      (recheck-path path))))

(defn cut-node [path node-id]
  (state/with-single-record
    (fn []
      (state/remove-node path node-id)
      (recheck-path path)
      (toggle-pasting node-id))))

(defn delete []
  (toggle-pasting nil))

(defn undo []
  (state/undo!))

(defn redo []
  (state/redo!))
