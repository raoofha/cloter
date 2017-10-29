(ns app.a
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]))

(defn main []
  (system.io/init)
  #_(go-loop [prompt true]
    (when prompt (io/prompt [:span "a> "]))
    (let [l (io/<? (io/readln))]
      (do
        (when-not (= l "")
          (io/writelnn l))
        (recur (not (= l ""))))))
  (go
    (io/write "hello world")
    (io/write (fn []
                [:div {:style {:background-color :grey :color :black}} 
                 [:input {:placeholder "don't type here"}]]
                ))
    )
  )
