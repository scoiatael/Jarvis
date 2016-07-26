(ns jarvis.views.components.code-box
  (:require [re-com.core :as rc]
            [garden.color :as color]
            [jarvis.views.font :as font]
            [jarvis.views.colors.solarized :as sol]
            [jarvis.views.components.paster :as past]
            [jarvis.util.core :as util]))

(def ^:private marked-color
  (-> sol/green
      color/as-hsl
      (color/desaturate 75)
      (color/lighten 50)
      color/as-hex))

(def ^:private dont-bubble util/dont-bubble)
(defn- attrs [o]
  (let [id (:id o)
        parent-id (:path o)
        on-click (:on-click o #())
        on-hover (:on-hover o #())]
    {:on-click (partial dont-bubble #(on-click id parent-id))
     :on-mouse-over (partial dont-bubble #(on-hover :over id parent-id))
     :on-mouse-out (partial dont-bubble #(on-hover :out id parent-id))}))

(defn- error-tooltip [errors]
  (->> errors seq (map name) (clojure.string/join ", ")))

(defn- render-errors [errors]
  (let [has-errors? (not (empty? errors))]
    (if has-errors?
      [rc/md-circle-icon-button
       :tooltip (error-tooltip errors)
       :style {:z-index 1
               :color sol/red}
       :size :smaller
       :class "btn-danger"
       :md-icon-name "zmdi-alert-triangle"]
      [:div])))

(defn- style [o color]
  (let [marked (:marked o)
        base {:color color
              :text-align "center"
              :transition "0.5s"
              :font-family font/code}]
    (into base (if marked {:background-color marked-color} {}))))

(defn- normal-box-style [color]
  {:border (str "solid 3px " color)
   :border-radius "2px"
   :height "100%"
   :padding "5px"
   :margin "0.5em"})

(defn- concise-box-style [color]
  {:border (str "solid 1px " color)
   ;; :border-top "0px"
   ;; :border-bottom "0px"
   ;; :border-radius "0.8em"
   :height "100%"
   :padding "0.2em"})

(defn code-box [o code color]
  (let [errors (:errors o)]
    [rc/box
     :attr (attrs o)
     :size "0 1 auto"
     :style (into (style o color)
                  (if (:concise o)
                    (concise-box-style color)
                    (normal-box-style color)))
     :child  [rc/v-box
              :size "0 1 auto"
              :align :center
              :children [[render-errors errors] code]]]))

(defn- error-text [errors code]
  [rc/button
   :label code
   :class "btn-danger"
   :tooltip (error-tooltip errors)])

(defn code-text [o code color]
  (let [errors (:errors o)
        hover-modifier (if (:paster o) #(dissoc % :on-mouse-out :on-mouse-over) identity)]
    [rc/box
     :attr (-> o
               attrs
               hover-modifier)
     :align :center
     :style (style o color)
     :child  (if (empty? errors) code [error-text errors code])]))

(defn paster [o pos]
  (let [attr (-> o
                 (into {:id {:after pos}})
                 (dissoc :on-hover)
                 attrs
                 (dissoc :on-mouse-over)
                 (dissoc :on-mouse-out))
        style {:background-color marked-color
               :color "black"}]
    [past/small
     :attr attr
     :disabled? (not (:marked o))
     :style style]))
