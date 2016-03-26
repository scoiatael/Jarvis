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
  [check-schema-mw
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
   r/update-suggestions)

  (register-handler
   :edit-elem-changed
   middlewares
   r/modal->code)

  (register-handler
   :node-clicked
   middlewares
   r/node-paste-or-cut)

  (register-handler
   :node-hover
   middlewares
   (fn [db [ev node]]
     (case ev
       :over (r/mark-node db [node])
       :out (r/unmark-node db [node]))))

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
   [middlewares (path :pasting)]
   (constantly nil))

  (register-handler
   :icon-undo-clicked
   middlewares
   do-nothing)

  (register-handler
   :icon-file-clicked
   middlewares
   r/open-file)

  (register-handler
   :icon-minus-clicked
   middlewares
   r/pop-code)

  (register-handler
   :namespace-function-clicked
   middlewares
   r/push-namespaced-fn)

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
   :push-file
   middlewares
   r/push-file)
  )
