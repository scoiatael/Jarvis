(ns jarvis.views.components.code_boxes
  (:require [re-com.core :as rc]
            [re-com.box :refer [flex-flow-style]]
            [garden.color :as color]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.syntax.walk :as walk]
            [jarvis.views.font :as font]
            [jarvis.util.core :as util]))

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

(def dont-bubble util/dont-bubble)

(defn- attrs [o]
  (let [id (:id o)
        parent-id (:path o)
        on-click (:on-click o #())
        on-hover (:on-hover o #())]
    {:on-click (partial dont-bubble #(on-click id parent-id))
     :on-mouse-over (partial dont-bubble #(on-hover :over id parent-id))
     :on-mouse-out (partial dont-bubble #(on-hover :out id parent-id))}))

(def ^:private marked-color
  (-> sol/green color/as-hsl (color/desaturate 75) (color/lighten 50) color/as-hex))

(defn- paster [o pos]
  [rc/md-circle-icon-button
   :attr (attrs (dissoc (into o {:id {:after pos}}) :on-hover))
   :md-icon-name "zmdi-format-color-fill"
   :size (or (:size o) :regular)
   :style {:background-color marked-color
           :color "black"}])

(defn- style [o color]
  (let [marked (:marked o)
        base {:color color
              :text-align "center"
              :transition "0.5s"
              :font-family font/code}]
    (into base (if marked {:background-color marked-color} {}))))

(defn push-id [o & args]
  (conj o
        {:path (apply conj (:path o) args)}))

(defn- code-box [o code color]
  (let [errors (:errors o)]
    [rc/box
     :attr (attrs o)
     :size "0 1 auto"
     :style (into (style o color)
                  {:border (str "solid 3px " color)
                   :border-radius "2px"
                   :height "100%"
                   :padding "5px"
                   :margin "0.5em"})
     :child  [rc/v-box
              :size "0 1 auto"
              :align :center
              :children [[render-errors errors] code]]]))

(defn- error-text [errors code]
  [rc/button
   :label code
   :class "btn-danger"
   :tooltip (str errors)])

(defn- code-text [o code color]
  (let [errors (:errors o)]
    [rc/box
     :attr (attrs o)
     :align :center
     :style (style o color)
     :child  (if (empty? errors) code [error-text errors code])]))

(defn- dissoc-errors [o] (dissoc o :errors))

(def ^:private type->color {:keyword sol/yellow
                            :symbol sol/base01
                            :number sol/green
                            :string sol/magenta
                            :list sol/violet
                            :map sol/blue
                            :vector sol/cyan
                            :misc sol/red})

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

(defn- mapcat-indexed [f coll]
  (apply concat (map-indexed f coll)))

(defn- interpose-paster [paster-component children]
  (let [children-vec (into [] children)]
    (mapcat-indexed (fn [index item] [(paster-component index) item]) (conj children-vec nil))))

;; Recursive types
(declare render)
(defn- render-seq [o k c]
  (let [children (->> k (map (partial render (dissoc-errors o))))
        render-paster (:paster o)
        paster-component (fn [p] [paster (push-id o (:id o)) p])]
    (code-box o [rc/h-box
                 :size "0 1 auto"
                 :align :center
                 :style (flex-flow-style "row wrap")
                 :gap sep
                 :children (if render-paster (interpose-paster paster-component children) children)] c)))

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

(defn- render-tuple-seq [o s c]
  (let [children (map #(render-tuple (dissoc-errors o)
                                     (->> % first)
                                     (->> % last))
                      s)]
    (code-box o
              [rc/h-box
               :size "0 1 auto"
               :style (flex-flow-style "row wrap")
               :gap sep
               :children  children]
              c)))

(defn- render-map [o k]
  ;; (util/log! k)
  (let [par (partition 2 k)
        sorted (sort-by #(-> % first walk/info :id) par)
        children (->> sorted
                      (map
                       #(list (->> % first (render (dissoc-errors o)))
                              (->> % last (render (dissoc-errors o))))))
        render-paster (:paster o)
        paster-component (fn [p] [paster (into (push-id o (:id o)) {:size :smaller}) p])]
    (render-tuple-seq
     o
     (if render-paster  (conj (into [] children) [(paster-component 0) (paster-component 1)]) children)
     (type->color :map))))

(defn- render-misc [o k t]
  (util/error! "Unknown value" k " of " t)
  (code-box o (str k) (type->color :misc)))

(defn render [o code]
  {:pre (walk/is-info? code)}
  (let [value (walk/value code)
        info (walk/info code)
        type (:type info)
        errors (info :errors)
        id (:id info)
        opts (conj (push-id o (:id o)) info)]
    (case type
      :bool (render-keyword opts value)
      :nil (render-nil opts value)
      :vector [render-vector opts value]
      :keyword [render-keyword opts value]
      :symbol [render-symbol opts value]
      :number [render-number opts value]
      :string [render-string opts value]
      :list [render-list opts value]
      :map [render-map opts value]
      nil [render-nil opts value]
      [render-misc opts value type])))
