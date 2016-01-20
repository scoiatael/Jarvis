(ns jarvis.views.render
  (:require [re-com.core :as rc]
            [re-com.box :refer [flex-flow-style]]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.views.font :as font]
            [jarvis.util.logger :as util]))

(defn- code-box [o code color]
  (let [on-click (:on-click o)
        on-hover (:on-hover o)]
    [rc/box
     :attr {:on-click on-click
            :on-mouse-over on-hover}
     :size "0 1 auto"
     :style {:border (str "solid 2px " color)
             :border-radius "2px"
             :height "100%"
             :padding "5px"
             :text-align "center"
             :font-family font/code
             :margin "0.5em"}
     :child  code]))

(defn- code-text [o code color]
  (let [on-click (:on-click o)
        on-hover (:on-hover o)]
    [rc/box
     :attr {:on-click on-click
            :on-mouse-over on-hover}
     :align :center
     :style {:color color
             :text-align "center"
             :font-family font/code}
     :child  code]))

(def ^:private type->color {
                            :keyword sol/yellow
                            :symbol sol/base01
                            :number sol/green
                            :string sol/magenta
                            :list sol/violet
                            :map sol/blue
                            :vector sol/cyan
                            :misc sol/red
                            })

;; Primitives
(defn- render-nil [o _] (code-text o "nil" (type->color :keyword)))
(defn- render-keyword [o k] (code-text o (str k) (type->color :keyword)))
(defn- prettify-symbol [s]
  (let [stringified (str s)]
    (case (-> stringified clojure.string/trim)
      "quote" "'"
      stringified)))
(defn- render-symbol [o k] (code-text o (prettify-symbol k) (type->color :symbol)))
(defn- render-number [o k] (code-text o k (type->color :number)))
(defn- render-string [o k] (code-text o (str "\"" k "\"") (type->color :string)))

(def ^:private sep "0.5em")

;; Recursive types
(declare render)
(defn- render-seq [o k c]
  (code-box o [rc/h-box
               :size "0 1 auto"
               :algin :center
               :style (flex-flow-style "row wrap")
               :gap sep
               :children (->> k (map (partial render o)))] c))

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
                                     :style (flex-flow-style "row wrap")
                                     :gap sep
                                     :children (->> k (map (partial render-tuple o)))] (type->color :map)))
(defn- render-misc [o k t]
  (print "Unknown value" k " of " t)
  (code-box o k (type->color :misc)))

(defn render [o code]
  (let [value (:value code)
        type (:type code)]
    (case type
      :nil (render-nil o value)
      :vector [render-vector o value]
      :keyword [render-keyword o value]
      :symbol [render-symbol o value]
      :number [render-number o value]
      :string [render-string o value]
      :list [render-list o value]
      :map [render-map o value]
      [render-misc o value type])))
