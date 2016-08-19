(ns app.dialog
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.logger :as util]))

(def ^:private electron (nodejs/require "electron"))
(def ^:private dialog (.-dialog electron))

(defn nrepl-error! [err]
  (.showErrorBox dialog
                 "nREPl error"
                 "An error occured during nREPL startup. To evaluate functions, you need to have Leiningen installed. You can download it from http://leiningen.org/ If problems persist, contact czapl.luk@gmail.com"))
