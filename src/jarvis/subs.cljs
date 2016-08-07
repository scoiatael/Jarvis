(ns jarvis.subs
  (:require [reagent.ratom :as ra :refer-macros [reaction]]
            [re-frame.core :as r-f :refer [register-sub]]
            [jarvis.state.core :as s]))

(defn register! []

  (register-sub
   :can-undo?
   (constantly (reaction false)))

  (register-sub
   :pasting?
   (fn [db _]
     (reaction (:pasting @db))))

  (register-sub
   :focus?
   (fn [db _]
     (reaction (:focus @db))))

  (register-sub
   :context-actions?
   (fn [db _]
     (reaction (or
                (:pasting @db)
                (:focus @db)))))

  (register-sub
   :tab
   (fn [db _]
     (reaction (:tab @db))))

  (register-sub
   :defs
   (fn [db _]
     (reaction (s/defs @db))))

  (register-sub
   :scratch
   (fn [db _]
     (reaction (s/scratch @db))))

  (register-sub
   :error
   (fn [db _]
     (reaction (:error @db))))

  (register-sub
   :modal
   (fn [db _]
     (reaction (:modal @db))))

  (register-sub
   :status
   (fn [db _]
     (reaction [(:nrepl-connection @db) (:pasting @db)])))

  (register-sub
   :suggestions
   (fn [db _]
     (reaction (:suggestions @db))))
  )
