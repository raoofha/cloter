(ns app.coffee
  (:require-macros
   [system.io :refer [<? defmain]]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [clojure.core.async]))

(defmain []
  (system.io/init)
  (loop []
    (io/prompt "> ")
    (let [l (<? (io/readln))]
      (io/writelnn (js/String (js/eval (js/CoffeeScript.compile l #js {:bare true}))))
      (recur))))
