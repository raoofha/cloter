(ns app.counter
  (:require [system.io :as io :refer-macros [<? defmain wait]]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [reagent.core :as r]))

(defmain []
  (let [state (r/atom
               {:app-id (io/new-app-id)
                :count 0
                :events [[:+1 #(update % :count inc)]
                         [:-1 #(update % :count dec)]]})
        root (fn [] [:div 
                     [:button {:on-click #(io/dispatch {:type :-1 :app-id (:app-id @state)})} "-"]
                     (:count @state)
                     [:button {:on-click #(io/dispatch {:type :+1 :app-id (:app-id @state)})} "+"]
                     ])]
    (system.io/init {:root root})
    (system.io/wait state)))
