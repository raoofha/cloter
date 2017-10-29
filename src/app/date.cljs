(ns app.date
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]))

(defn main []
  (io/write (js/String (js/Date.)))
  (go))
