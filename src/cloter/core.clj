(ns cloter.core
  (:require [cloter.view :as view]
            [cloui.core]))

(def state (atom {}))

(defn render []
  (cloui.core/-render view/root)
  )

(defn -main []
  ;(cloui.core/-run render)
  )
