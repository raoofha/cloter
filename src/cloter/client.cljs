(ns cloter.client
  ;(:require-macros [app.cljs])
  (:require [cloter.config :as config]
            [app.console]
            [app.cljs]))

(defn ^:export init []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode"))
  (app.console/main)
  ;(app.cljs/main)
  #_(r/render
   [#(into [:div] @io/*display*)]
   ;[#(into [:div] (do (convert-space->nbsp @io/*display*)))]
   (.getElementById js/document "app")))
