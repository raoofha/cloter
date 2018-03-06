(ns app.clock
  (:require [system.io :as io :refer-macros [<? defmain wait]]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [reagent.core :as r]))

(defmain []
  (let [state (r/atom
               {:time (js/Date.)
                :events [[:time #(assoc %1 :time (js/Date.)
                                        #_(let [d (js/Date.)]
                                          (str (.getHours d) ":" (.getMinutes d) ":" (.getSeconds d))))]]})
        hand (fn [{deg :deg size :size}]
                     [:div {:style {:width 2 :height size 
                                    :left 50 :top 0
                                    :position :absolute
                                    :background-color "white"
                                    :transform-origin "0px 50px"
                                    :transform (str "rotate(" deg "deg)")}}
                      ])
        root (fn [] [:div {:style {:width 100 :height 100
                                   :position :relative}} ;(:time @state)
                     [hand {:deg (* 6 (.getSeconds (:time @state)))
                            :size 50}]
                     [hand {:deg (* 6 (.getMinutes (:time @state)))
                            :size 40
                            }]
                     [hand {:deg (* 30 (mod (.getHours (:time @state)) 12))
                            :size 30
                            }]
                     ])]
    (system.io/init {:root root :time true})
    ;(system.io/init {:root root :fullscreen true :time true})
    (system.io/wait state)))
