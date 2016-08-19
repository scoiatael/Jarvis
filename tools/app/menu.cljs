(ns app.menu
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.logger :as util]))

(def Menu (.-Menu (nodejs/require "electron")))
(def edit-submenu {:label "Edit",
                   :submenu [{:role "undo"}
                             {:role "redo"}
                             {:type "separator"}
                             {:role "cut"}
                             {:role "copy"}
                             {:role "paste"}
                             {:role "pasteandmatchstyle"}
                             {:role "delete"}
                             {:role "selectall"}]})

(def view-submenu {:label "View"
                   :submenu [{:label "Reload",
                              :accelerator "CmdOrCtrl+R"
                              :click (fn [item focusedWindow] (when focusedWindow (.reload focusedWindow)))}
                             {:label "Toggle Developer Tools",
                              :accelerator (if (= (.-platform nodejs/process) "darwin") "Alt+Command+I" "Ctrl+Shift+I")
                              :click (fn [item focusedWindow] (if focusedWindow (-> focusedWindow .-webContents .toggleDevTools)))}
                             {:type "separator"}
                             {:role "togglefullscreen"}]})

(def window-submenu {:role "window"
                     :submenu [{:role "minimize"}
                               {:role "close"}]})

(def help-submenu {:role "help"
                   :submenu [{:label "Learn more"
                              :click (fn [] (-> "electron"
                                                nodejs/require
                                                .-shell
                                                (.openExternal "http://scoiatael.github.io/Jarvis")))}
                             {:label "Open logs"
                              :click (fn [] (-> "electron"
                                                nodejs/require
                                                .-shell
                                                (.openItem util/logpath)))}]})

(def app-submenu {:label "Jarvis"
                  :submenu [{:role "about"}
                            {:role "separator"}
                            {:role "services"
                             :submenu []}
                            {:role "separator"}
                            {:role "hide"}
                            {:role "hideothers"}
                            {:role "unhide"}
                            {:role "separator"}
                            {:role "quit"}]})

(def standard-template [edit-submenu
                        view-submenu
                        window-submenu
                        help-submenu])

(def darwin-template [app-submenu
                      (update-in edit-submenu
                                 [:submenu]
                                 conj
                                 {:type "separator"}
                                 {:label "Speech"
                                  :submenu [{:role "startspeaking"}
                                            {:role "stopspeaking"}]})
                      view-submenu
                      (update-in window-submenu
                                 [:submenu]
                                 conj
                                 {:label "Close"
                                  :accelerator "CmdOrCtrl+W"
                                  :role "close"}
                                 {:label "Minimize"
                                  :accelerator "CmdOrCtrl+M"
                                  :role "minimize"}
                                 {:label "Zoom"
                                  :role "zoom"}
                                 {:type "separator"}
                                 {:label "Bring All to Front"
                                  :role "front"})
                      help-submenu])

(defn init []
  (let [template (if (= (.-platform nodejs/process) "darwin") darwin-template standard-template)
        menu (.buildFromTemplate Menu (clj->js template))]
    (.setApplicationMenu Menu menu)))
