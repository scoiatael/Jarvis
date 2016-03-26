(ns jarvis.subs
  (:require [reagent.ratom :as ra :refer-macros [reaction]]
            [re-frame.core :as r-f :refer [register-sub]]
            [jarvis.state.core :as s]))

(defn register! []

  (register-sub
   :can-remove?
   (fn [db _]
     (reaction (if (:nodes @db)
                 (> (s/nodes-length @db) 0)
                 false))))

  (register-sub
   :can-undo?
   (constantly (reaction false)))

  (register-sub
   :pasting?
   (fn [db _]
     (reaction (:pasting @db))))

  (register-sub
   :codes
   (fn [db _]
     (reaction (s/nodes @db))))

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
     (reaction true)))

  (register-sub
   :suggestions
   (fn [db _]
     (reaction (:suggestions @db))))
  )
