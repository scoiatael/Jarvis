(defproject jarvis "0.1.0-SNAPSHOT"
  :description "Visual Programming Environment for Clojure"
  :url "http://github.com/scoiatael/Jarvis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.183"]
                 [org.clojure/core.async "0.2.385"]
                 [re-com "0.8.3"]
                 [historian "1.1.0"]
                 [re-frame "0.7.0"]
                 [figwheel "0.5.4-7"]
                 [prismatic/schema "1.1.3"]
                 [reagent "0.5.1"]
                 [org.clojure/tools.reader "1.0.0-alpha1"]
                 [garden "1.3.2"]
                 [ring/ring-core "1.5.0"]]

  :min-lein-version "2.5.3"

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-externs "0.1.3"]]

  :clean-targets [:target-path
                  "app/js/out"
                  "app/js/renderer.js"
                  "app/js/front.js"
                  "figwheel_server.log"
                  "npm-debug.log"]

  :figwheel {:http-server-root "app"
             :port 3449
             :ring-handler figwheel-middleware/app}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :source-paths ["tools/server"]

  :cljsbuild
  {:builds
   [{:id "main"
     :source-paths ["tools"]
     :compiler {:output-to "app/js/main.js"
                :main "app.core"
                :warnings true
                :target :nodejs
                :optimizations :simple}}
    {:id "release"
     :source-paths ["src"]
     :compiler {:output-to "app/js/app.js"
                :main "jarvis.core"
                :externs ["app/js/externs.js"]
                :warnings false
                :optimizations :advanced}}
    {:id "dev"
     :source-paths ["src"]
     :compiler {:main "jarvis.core"
                :output-to "app/js/app.js"
                :output-dir "app/js/compiled/out"
                :asset-path "js/compiled/out"
                :externs ["app/js/externs.js"]
                :output-wrapper true
                :source-map-timestamp true}}]}

  :profiles
  {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                        [figwheel-sidecar "0.5.4-7"]]
         :plugins [[lein-figwheel "0.5.0"]
                   [cider/cider-nrepl "0.13.0-SNAPSHOT"]]}})
