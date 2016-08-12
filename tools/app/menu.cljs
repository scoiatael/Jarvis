(ns app.menu
  (:require [cljs.nodejs :as nodejs]
            [jarvis.util.logger :as util]))

(def Menu (.-Menu (nodejs/require "electron")))
(def standard-template [{:label "Edit",
                :submenu [{:role "undo"}
                          {:role "redo"}
                          {:type "separator"}
                          {:role "cut"}
                          {:role "copy"}
                          {:role "paste"}
                          {:role "pasteandmatchstyle"}
                          {:role "delete"}
                          {:role "selectall"}]}
               {:label "View"
                :submenu [{:label "Reload",
                           :accelerator "CmdOrCtrl+R"
                           :click (fn [item focusedWindow] (when focusedWindow (.reload focusedWindow)))}
                          {:label "Toggle Developer Tools",
                           :accelerator (if (= (.-platform nodejs/process) "darwin") "Alt+Command+I" "Ctrl+Shift+I")
                           :click (fn [item focusedWindow] (if focusedWindow (-> focusedWindow .-webContents .toggleDevTools)))}
                          {:type "separator"}
                          {:role "resetzoom"}
                          {:role "zoomin"}
                          {:role "zoomout"}
                          {:type "separator"}
                          {:role "togglefullscreen"}]}
               {:role "windows"
                :submenu [{:role "minimize"}
                          {:role "close"}]}
               {:role "help"
                :submenu [{:label "Learn more"
                           :click (fn [] (-> "electron"
                                            nodejs/require
                                            .-shell
                                            (.openExternal "http://scoiatael.github.io/Jarvis")))}]}])

(def darwin-template [{:label "Jarvis"
                       :submenu [{:role "about"}
                                 {:role "separator"}
                                 {:role "services"
                                  :submenu []}
                                 {:role "separator"}
                                 {:role "hide"}
                                 {:role "hideothers"}
                                 {:role "unhide"}
                                 {:role "separator"}
                                 {:role "quit"}]}
                      {:label "Edit",
                       :submenu [{:role "undo"}
                                 {:role "redo"}
                                 {:type "separator"}
                                 {:role "cut"}
                                 {:role "copy"}
                                 {:role "paste"}
                                 {:role "pasteandmatchstyle"}
                                 {:role "delete"}
                                 {:role "selectall"}
                                 {:type "separator"}
                                 {:label "Speech"
                                  :submenu [{:role "startspeaking"}
                                            {:role "stopspeaking"}]}]}
                      {:label "View"
                       :submenu [{:label "Reload",
                                  :accelerator "CmdOrCtrl+R"
                                  :click (fn [item focusedWindow] (when focusedWindow (.reload focusedWindow)))}
                                 {:label "Toggle Developer Tools",
                                  :accelerator (if (= (.-platform nodejs/process) "darwin") "Alt+Command+I" "Ctrl+Shift+I")
                                  :click (fn [item focusedWindow] (if focusedWindow (-> focusedWindow .-webContents .toggleDevTools)))}
                                 {:type "separator"}
                                 {:role "resetzoom"}
                                 {:role "zoomin"}
                                 {:role "zoomout"}
                                 {:type "separator"}
                                 {:role "togglefullscreen"}]}
                      {:role "windows"
                       :submenu [{:role "minimize"}
                                 {:role "close"}
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
                                  :role "front"}]}
                      {:role "help"
                       :submenu [{:label "Learn more"
                                  :click (fn [] (-> "electron"
                                                    nodejs/require
                                                    .-shell
                                                    (.openExternal "http://scoiatael.github.io/Jarvis")))}
                                 {:label "Open logs"
                                  :click (fn [] (-> "electron"
                                                   nodejs/require
                                                   .-shell
                                                   (.openItem util/logpath)))}]}])

(defn init []
  (let [template (if (= (.-platform nodejs/process) "darwin") darwin-template standard-template)
        menu (.buildFromTemplate Menu (clj->js template))]
    (.setApplicationMenu Menu menu)))
