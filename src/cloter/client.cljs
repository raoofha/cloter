(ns cloter.client
  ;(:require-macros [app.cljs])
  (:require [cloter.config :as config]
            [system.io]
            [app.console]
            [app.cljs]
            [app.js]
            [app.ts]
            [app.bs]
            [app.coffee]))

(defn ^:export init []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode"))
  (system.io/init)
  (app.console/main)
  ;(app.cljs/main)
  )
