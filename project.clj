(defproject cloter "0.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 ;[reagent "0.6.2"]
                 [reagent "0.7.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [hiccup "1.0.5"]
                 [environ "1.1.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :min-lein-version "2.5.3"
  ;:main cloter.server
  :clean-targets ^{:protect false} [".stuff" "target" "resources/public/.cljs-stuff"]

  :figwheel {:css-dirs ["resources/public"]
             :server-port   3000
             :server-logfile ".stuff/figwheel-server.log"}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.9"]
                   [figwheel-sidecar "0.5.15"]
                   [com.cemerick/piggieback "0.2.2"]
                   [org.clojure/tools.nrepl "0.2.13"]]
    :plugins      [[lein-figwheel "0.5.15"]]
    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src"]
     :figwheel     {}
     :compiler     {:main                 cloter.client
                    :output-to            "resources/public/cloter.js"
                    :output-dir           "resources/public/.cljs-stuff"
                    :asset-path           ".cljs-stuff/"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}
    {:id           "min"
     :source-paths ["src"]
     :compiler     {:main            cloter.client
                    :output-to       "docs/cloter.js"
                    :output-dir      "docs/.cljs-stuff-whitespace"
                    :optimizations   :whitespace
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}
    {:id           "min-simple"
     :source-paths ["src"]
     :compiler     {:main            cloter.client
                    :output-to       "docs/cloter.js"
                    :output-dir      "docs/.cljs-stuff-simple"
                    :optimizations   :simple
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}
    {:id           "min-advanced"
     :source-paths ["src"]
     :compiler     {:main            cloter.client
                    :output-to       "docs/cloter.js"
                    :output-dir      "docs/.cljs-stuff-advanced"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
