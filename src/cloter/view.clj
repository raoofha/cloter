(ns cloter.view
  (:import (javafx.scene Scene)
           (javafx.scene.control Button)
           (javafx.scene.layout StackPane)
           (javafx.stage Stage)))

(def state (atom {:text "My button"}))

(defn root []
  [Stage {:title "cloter: hello world"}
   [Scene {:width 800 :height 800}
    [StackPane
     [Button (:text @state)]
     ]
    ]
   ]
  )
