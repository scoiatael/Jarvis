(ns jarvis.util.render)

(defn dont-bubble [f ev]
  (do
    (if (nil? (.-stopPropagation ev))
      (set! (.-cancelBubble ev) true)
      (.stopPropagation ev))
    (f)))
