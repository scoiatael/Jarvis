(ns jarvis.util.logger)

(defn error! [& args] (.apply (.-error js/console) js/console (clj->js args)))

(defn log! [& args] (.apply (.-log js/console) js/console (clj->js args)))
