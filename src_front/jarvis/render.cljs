(ns jarvis.render
  (:require
   [re-com.core :as rc]
   [jarvis.pretty-print :as pp]))

(defn- code-box [o code color]
  (let [on-click (:on-click o)]
    [:div
     {:on-click on-click
      :style {:border "solid 5px"
              :border-color color
              :height "100%"
              :margin "1em"}}
     code]))

(def ^:private type->color {
                            :keyword "blue"
                            :symbol "green"
                            :number "yellow"
                            :string "orange"
                            :list "magenta"
                            :map "pink"
                            :vector "grey"
                            :misc "red"
                            })

;; Primitives
(defn- render-keyword [o k] (code-box o (pp/pretty-print k) (type->color :keyword)))
(defn- prettify-symbol [s]
  (case (clojure.string/trim s)
    "quote" "'"
    s))
(defn- render-symbol [o k] (code-box o (prettify-symbol (pp/pretty-print k)) (type->color :symbol)))
(defn- render-number [o k] (code-box o k (type->color :number)))
(defn- render-string [o k] (code-box o k (type->color :string)))

;; Recursive types
(declare render)
(defn- render-vector [o k] (code-box o (map (partial render o) k) (type->color :vector)))
(defn- render-list [o k] (code-box o (map (partial render o) k) (type->color :list)))
(defn- render-tuple [o k] (let [f (first k)
                              s (second k)]
                          [:div (render o f) [:i {:class "zmdi zmdi-hc-fw-rc zmdi-arrow-right"}] (render o s)]))
(defn- render-map [o k] (code-box o (map (partial render-tuple o) k) (type->color :map)))
(defn- render-misc [o k t]
  (print "Unknown value" k " of " t)
  (code-box o (pp/pretty-print k) (type->color :misc)))

(defn render [o code]
  (let [value (:value code)
        type (:type code)]
    (case type
      :vector [render-vector o value]
      :keyword [render-keyword o value]
      :symbol [render-symbol o value]
      :number [render-number o value]
      :string [render-string o value]
      :list [render-list o value]
      :map [render-map o value]
      [render-misc o value type])))
