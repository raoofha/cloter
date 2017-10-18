(defproject cloter "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.443"]]
  :min-lein-version "2.5.3"
  :main cloter.core
  :aot [cloui.core] ;:all
  :clean-targets ^{:protect false} ["target"])
