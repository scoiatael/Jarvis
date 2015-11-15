(ns jarvis.render
  (:require
   [re-com.core :as rc]
   [jarvis.pretty-print :as pp]))

(defn- code-box [code color] [:div
                              {:style
                               {:border "solid 5px"
                                :border-color color
                                :height "100%"
                                :margin "1em"}}
                              code])

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
(defn- render-keyword [k] (code-box (pp/pretty-print k) (type->color :keyword)))
(defn- prettify-symbol [s]
  (case (clojure.string/trim s)
    "quote" "'"
    s))
(defn- render-symbol [k] (code-box (prettify-symbol (pp/pretty-print k)) (type->color :symbol)))
(defn- render-number [k] (code-box k (type->color :number)))
(defn- render-string [k] (code-box k (type->color :string)))

;; Recursive types
(declare render)
(defn- render-vector [k] (code-box (map render k) (type->color :vector)))
(defn- render-list [k] (code-box (map render k) (type->color :list)))
(defn- render-tuple [k] (let [f (first k)
                              s (second k)]
                          [:div (render f) [:i {:class "zmdi zmdi-hc-fw-rc zmdi-arrow-right"}] (render s)]))
(defn- render-map [k] (code-box (map render-tuple k) (type->color :map)))
(defn- render-misc [k t]
  (print "Unknown value" k " of " t)
  (code-box (pp/pretty-print k) (type->color :misc)))

(defn render [code] (let [value (:value code)
                          type (:type code)]
                      (case type
                        :vector [render-vector value]
                        :keyword [render-keyword value]
                        :symbol [render-symbol value]
                        :number [render-number value]
                        :string [render-string value]
                        :list [render-list value]
                        :map [render-map value]
                        [render-misc value type])))
