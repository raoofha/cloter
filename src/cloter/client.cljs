(ns cloter.client
  (:require system.io
            app.console ;app.cljs app.js app.ts app.bs app.coffee app.a app.date
            ))

(defn ^:export init []
  (when goog.DEBUG
    (enable-console-print!)
    (println "dev mode"))
  (app.console/main)
  ;(app.cljs/main)
  )
