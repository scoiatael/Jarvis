(ns jarvis.util.promise
  (:require  [cljs.core.async :as async :refer [<! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn then [p & fns]
  (let [apply-fns (fn [v]
                    (doseq [f fns]
                      (f v)))]
    (go (-> p
            <!
            apply-fns))))

