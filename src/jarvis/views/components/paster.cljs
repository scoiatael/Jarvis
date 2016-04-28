(ns jarvis.views.components.paster
  (:require [re-com.core :refer [v-box box h-box input-textarea gap single-dropdown] :as rc]))

(defn small [& args]
  (apply conj
         [rc/md-icon-button
          :md-icon-name "zmdi-download"]
         args))

(defn big [& args]
  (apply conj 
         [rc/button
          :label [:span "Paste " [:i.zmdi.zmdi-hc-fw-rc.zmdi-download]]]
          args))
