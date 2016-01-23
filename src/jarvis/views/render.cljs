(ns jarvis.views.render
  (:require [re-com.core :as rc]
            [re-com.box :refer [flex-flow-style]]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.syntax.walk :as walk]
            [jarvis.views.font :as font]
            [jarvis.util.logger :as util]))

(defn- render-errors [errors]
  (let [has-errors? (not (empty? errors))]
    (if has-errors?
      [rc/md-circle-icon-button
       :tooltip (str errors)
       :style {:z-index 1
               :color sol/red}
       :size :smaller
       :class "btn-danger"
       :md-icon-name "zmdi-alert-triangle"]
      [:div])))

(defn- code-box [o code color]
  (let [on-click (:on-click o)
        on-hover (:on-hover o)
        errors (:errors o)]
    [rc/box
     :attr {:on-click on-click
            :on-mouse-over on-hover}
     :size "0 1 auto"
     :style {:border (str "solid 3px " color)
             :border-radius "2px"
             :height "100%"
             :padding "5px"
             :text-align "center"
             :font-family font/code
             :margin "0.5em"}
     :child  [rc/v-box
              :children [[render-errors errors] code]]]))

(defn- code-text [o code color]
  (let [on-click (:on-click o)
        on-hover (:on-hover o)
        errors (:errors o)]
    [rc/box
     :attr {:on-click on-click
            :on-mouse-over on-hover}
     :align :center
     :style {:color color
             :text-align "center"
             :font-family font/code}
     :child  [rc/v-box
              :children [code [render-errors errors]]]]))

(defn- dissoc-errors [o] (dissoc o :errors))

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
(defn- render-number [o k] (code-text o (str k) (type->color :number)))
(defn- render-string [o k] (code-text o (str "\"" k "\"") (type->color :string)))

(def ^:private sep "0.5em")

;; Recursive types
(declare render)
(defn- render-seq [o k c]
  (code-box o [rc/h-box
               :size "0 1 auto"
               :align :center
               :style (flex-flow-style "row wrap")
               :gap sep
               :children (->> k (map (partial render (dissoc-errors o))))] c))

(defn- render-vector [o k] (render-seq o k (type->color :vector)))

(defn- render-list [o k] (render-seq o k (type->color :list)))

(defn- render-tuple [o f s]
  [rc/v-box
   :size "0 1 auto"
   :children [f
              [rc/md-icon-button
               :md-icon-name "zmdi-long-arrow-down"
               :disabled? true]
              s]])

(defn- render-tuple-seq [o s c] (code-box o
                                          [rc/h-box
                                           :size "0 1 auto"
                                           :style (flex-flow-style "row wrap")
                                           :gap sep
                                           :children  (map
                                                       #(render-tuple (dissoc-errors o)
                                                                      (->> % first)
                                                                      (->> % last))
                                                       s)]
                                          c))

(defn- render-map [o k] (render-tuple-seq
                         o
                         (->> k
                              (map
                               #(list (->> % first (render (dissoc-errors o)))
                                      (->> % last (render (dissoc-errors o))))))
                         (type->color :map)))

(defn- render-record [o k] (render-tuple-seq
                            o
                            (->> k
                                 (map
                                  #(list (->> % first (render-symbol (dissoc-errors o)))
                                         (->> % last (render (dissoc-errors o))))))
                            (type->color :map)))

(defn- render-misc [o k t]
  (util/error! "Unknown value" k " of " t)
  (code-box o (str k) (type->color :misc)))

(defn render [o code]
  ;; pre satisfies? Info code
  (let [value (walk/value code)
        type (-> code walk/info :type)
        errors (-> code walk/info :errors)
        opts (update-in o [:errors] #(into % errors))]
    (case type
      :bool (render-keyword opts value)
      :nil (render-nil opts value)
      :vector [render-vector opts value]
      :keyword [render-keyword opts value]
      :symbol [render-symbol opts value]
      :number [render-number opts value]
      :string [render-string opts value]
      :list [render-list opts value]
      ;; :record [render-record opts value]
      :map [render-map opts value]
      [render-misc opts value type])))
