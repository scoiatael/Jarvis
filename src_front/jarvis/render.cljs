(ns jarvis.render
  (:require
   [re-com.core :as rc]
   [jarvis.pretty-print :as pp]
   [jarvis.colors.solarized :as sol]
   [jarvis.font :as font]))

(defn- code-box [o code color]
  (let [on-click (:on-click o)
        on-hover (:on-hover o)]
    [rc/box
     :attr {:on-click on-click
            :on-mouse-over on-hover}
     :style {:border (str "solid 2px " color)
             :border-radius "2px"
             :height "100%"
             :padding "5px"
             :text-align "center"
             :font-family font/code
             :margin "1em"}
     :child  code]))

(def ^:private type->color {
                            :keyword sol/yellow
                            :symbol sol/orange
                            :number sol/green
                            :string sol/magenta
                            :list sol/violet
                            :map sol/blue
                            :vector sol/cyan
                            :misc sol/red
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
(defn- render-seq [o k c]
  (code-box o [rc/h-box
               :children (map (partial render o) k)] c))

(defn- render-vector [o k] (render-seq o k (type->color :vector)))

(defn- render-list [o k] (render-seq o k (type->color :list)))

(defn- render-tuple [o k] (let [f (first k)
                              s (second k)]
                            [rc/v-box
                             :children [(render o f)
                                        [rc/md-icon-button
                                         :md-icon-name "zmdi-long-arrow-down"
                                         :disabled? true]
                                        (render o s)]]))

(defn- render-map [o k] (code-box o [rc/h-box
                                     :children (map (partial render-tuple o) k)] (type->color :map)))
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
