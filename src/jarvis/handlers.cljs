(ns jarvis.handlers
  (:require [re-frame.core :as r-f :refer [register-handler dispatch after path]]
            [jarvis.reducers :as r]
            [schema.core :as s]
            [jarvis.state.core :as st]))

;; -- Helpers ------------
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

;; Event handlers change state, that's their job. But what heppens if there's
;; a bug and they corrupt this state in some subtle way? This middleware is run after
;; each event handler has finished, and it checks app-db against a schema.  This
;; helps us detect event handler bugs early.
(def ^:pricate check-schema-mw (after (partial check-and-throw st/schema)))

(def ^:private middlewares
  [
   (when goog.DEBUG check-schema-mw)
   r-f/debug
   r-f/trim-v])

(defn- do-nothing [db _ ]
  db)

(defn register! []

  ;; -- User Event Handlers ----------------------

  (register-handler
   :initialise-db
   middlewares
   r/initialise-db)

  (register-handler
   :repl-connected
   middlewares
   (fn [db ev]
     (-> db
         (assoc-in [:nrepl-connection] true)
         (r/update-suggestions ev))))

  (register-handler
   :edit-elem-changed
   middlewares
   r/modal->code)

  (register-handler
   :add-elem-clicked
   middlewares
   r/modal->code)

  (register-handler
   :node-clicked
   middlewares
   (fn [db [node path]]
     (if (:pasting db)
       (r/paste-node db [node path])
       (r/mark-focused db [node path]))))

  (register-handler
   :def-clicked
   middlewares
   (fn [db ev]
     (-> db
         (r/node-push-scratch ev)
         (r/switch-to-tab [:edit]))))

  (register-handler
   :scratch-paster-clicked
   middlewares
   r/node-push-pasting-scratch)

  (register-handler
   :icon-play-clicked
   middlewares
   (fn [db _]
     (let [action (case (st/context db)
                    :pasting r/eval-pasting
                    :focus r/eval-focus)]
       (-> db
           action
           (r/switch-to-tab [:eval])))))

  (register-handler
   :icon-copy-clicked
   middlewares
   (fn [db _]
     (let [[node-id _] (:focus db)]
       (r/set-pasting db node-id))))

  (register-handler
   :icon-cut-clicked
   middlewares
   (fn [db _]
     (let [[node-id path] (:focus db)]
       (-> db
           (r/cut-node [node-id path])
           r/unmark-focused))))

  (register-handler
   :node-hover
   r-f/trim-v
   (fn [db [ev node path]]
     (case ev
       :over (r/mark-node db [node path])
       :out (r/unmark-node db [node path]))))

  (register-handler
   :error-backdrop-clicked
   [middlewares (path :error)]
   (constantly nil))

  (register-handler
   :modal-backdrop-clicked
   [middlewares (path :modal)]
   (constantly nil))

  (register-handler
   :icon-plus-clicked
   [middlewares (path :modal)]
   (constantly true))

  (register-handler
   :icon-delete-clicked
   middlewares
   (fn [db _]
     (let [action (case (st/context db)
                    :focus #(r/cut-node % (st/context-id db))
                    identity)]
       (-> db
           action
           (assoc :pasting nil)
           r/unmark-focused))))

  (register-handler
   :icon-undo-clicked
   middlewares
   do-nothing)

  (register-handler
   :icon-file-clicked
   middlewares
   r/open-file)

  (register-handler
   :icon-save-clicked
   middlewares
   r/save-file)

  (register-handler
   :namespace-function-clicked
   middlewares
   r/push-namespaced-fn)

  (register-handler
   :tab-clicked
   middlewares
   r/switch-to-tab)

  ;; -- Lifecycle Event Handlers

  (register-handler
   :add-suggestion
   middlewares
   r/add-namespace-functions)

  (register-handler
   :add-error
   middlewares
   r/set-node-error)

  (register-handler
   :add-eval-info
   middlewares
   r/set-node-eval-info)

  (register-handler
   :push-file
   middlewares
   r/push-file)
  )
