(ns app.cljs
  (:require-macros
   [system.io :refer [<? defmain]]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [clojure.core.async :refer [<!]]))

(defmain []
  (system.io/init)
  (loop []
    (io/prompt "=> ")
    (let [l (<? (io/readln))]
      (binding [*print-fn* io/writeln
                *print-err-fn* io/writeln]
        (eval-str
         (empty-state)
         l ""
         {:eval js-eval :context :expr}
         (fn [res]
           (io/writelnn (pr-str (:value res))))))
      (recur))))
