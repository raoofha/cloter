(ns app.js
  (:require [system.io :as io :refer-macros [<? defmain]]))

(defmain []
  (system.io/init)
  (loop []
    (io/prompt "> ")
    (let [l (<? (io/readln))]
      (io/writelnn (js/String (js/eval l)))
      (recur))))
