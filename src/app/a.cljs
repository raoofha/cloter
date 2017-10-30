(ns app.a
  (:require [system.io :as io :refer-macros [<? defmain]]))

(defmain []
  (system.io/init)
  (io/write "hello world")
  (io/write (fn []
              [:span {:style {:background-color :grey :color :black}}
               [:input {:placeholder "don't type here"}]])))
