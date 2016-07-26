(ns jarvis.views.components.sexp
  (:require [re-com.core :as rc]
            [re-com.box :refer [flex-flow-style]]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.syntax.walk :as walk]
            [jarvis.util.core :as util]
            [jarvis.views.components.code-box :refer [code-text code-box paster]]))

(defn push-id [o & args]
  (conj o
        {:path (apply conj (:path o) args)}))

(defn- dissoc-errors [o] (dissoc o :errors))

(def ^:private type->color {:keyword sol/yellow
                            :symbol sol/base01
                            :reserved-symbol sol/blue
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
(defn- render-reserved-symbol [o k] (code-text o (prettify-symbol k) (type->color :reserved-symbol)))
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

(defn- render-stdout [out]
  [rc/v-box
   :children (map (fn [line] [rc/box
                             :style {:background-color sol/base02
                                     :padding-left "1em"
                                     :color sol/base2}
                             :child (str line)]) out)])

(defn- render-ex [ex]
  [rc/box
   :style {:background-color sol/red}
   :child (str ex)])

(defn- render-val [val]
  [rc/box
   :child (str val)])

(defn- render-info [info component]
  [rc/v-box
   :children [component
              [rc/gap :size "0.3em"]
              [render-stdout (:out info)]
              [render-ex (:ex info)]
              [render-val (:val info)]]])

(defn render [o code]
  {:pre [(walk/is-info? code)]}
  (let [value (walk/value code)
        info (walk/info code)
        type (:type info)
        errors (info :errors)
        id (:id info)
        opts (conj (push-id o (:id o)) info)
        eval-info (:eval info)
        component (case type
                    :bool (render-keyword opts value)
                    :nil (render-nil opts value)
                    :vector [render-vector opts value]
                    :keyword [render-keyword opts value]
                    :reserved-symbol [render-reserved-symbol opts value]
                    :symbol [render-symbol opts value]
                    :number [render-number opts value]
                    :string [render-string opts value]
                    :list [render-list opts value]
                    :map [render-map opts value]
                    nil [render-nil opts value]
                    [render-misc opts value type])]
    (if (nil? eval-info)
      component
      [render-info eval-info component])))
