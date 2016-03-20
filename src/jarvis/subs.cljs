(ns jarvis.subs
  (:require [reagent.ratom :as ra :refer-macros [reaction]]
            [re-frame.core :as r-f :refer [register-sub]]))

(defn register! []
  ;; FIXME: Temporary -> break it down!
  (register-sub
   :db-state
   (fn [db _]
     (reaction @db))))
