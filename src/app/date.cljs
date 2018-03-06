(ns app.date
  (:require [system.io :as io :refer-macros [<? defmain]]))

(defmain []
  (io/write (js/Date)))
