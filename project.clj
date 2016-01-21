(defproject jarvis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228" :exclusions [org.apache.ant/ant]]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [re-com "0.7.0-alpha1"]
                 [re-frame "0.5.0"]
                 [figwheel "0.4.0"]
                 [reagent "0.5.1"]
                 [org.clojure/tools.reader "1.0.0-alpha1"]
                 [garden "1.3.0-SNAPSHOT"]
                 [ring/ring-core "1.4.0"]]
  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-externs "0.1.3"]
            [lein-figwheel "0.5.0"]]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
  :source-paths ["tools/server"]
  :aliases {"npm-deps" ["trampoline" "shell" "npm" "install"]
            "startapp" ["trampoline" "shell" "npm" "start"]}
  :clean-targets [:target-path "app/js/out" "app/js/renderer.js" "app/js/front.js"]
  :cljsbuild {:builds
              {:renderer {:id "renderer"
                      :source-paths ["tools"]
                      :incremental true
                      :jar true
                      :assert true
                      :compiler {:output-to "app/js/renderer.js"
                                 :externs ["app/js/externs.js"
                                           "node_modules/closurecompiler-externs/path.js"
                                           "node_modules/closurecompiler-externs/process.js"]
                                 :main "app.core"
                                 :warnings true
                                 ;; :elide-asserts true
                                 :target :nodejs

                                 ;; no optimize compile (dev)
                                 ;;:optimizations :none
                                 ;; when no optimize uncomment
                                 ;;:output-dir "app/js/out"

                                 ;; simple compile (dev)
                                 :optimizations :simple

                                 ;; advanced compile (prod)
                                 ;; :optimizations :advanced

                                 ;; :source-map "app/js/test.js.map"
                                 :pretty-print true
                                 :output-wrapper true
                                 }}
               :main {:id "main"
                          :source-paths ["src"]
                          :incremental true
                          :jar true
                          :assert true
                          :compiler {:output-to "app/js/front.js"
                                     :externs ["app/js/externs.js"]
                                     :warnings true
                                     ;; :elide-asserts true

                                     ;; no optimize compile (dev)
                                     ;;:optimizations :none
                                     ;; when no optimize uncomment
                                     ;;:output-dir "app/js/out"

                                     ;; simple compile (dev)
                                     :optimizations :none

                                     ;; advanced compile (prod)
                                     ;; :optimizations :none

                                     :pretty-print true
                                     :output-wrapper true
                                     }}}}
  :figwheel {:http-server-root "app"
             :ring-handler figwheel-middleware/app
             :server-port 3449

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             ;; Load CIDER, refactor-nrepl and piggieback middleware
              :nrepl-middleware ["cider.nrepl/cider-middleware"
                                 "refactor-nrepl.middleware/wrap-refactor"
                                 "cemerick.piggieback/wrap-cljs-repl"]})
