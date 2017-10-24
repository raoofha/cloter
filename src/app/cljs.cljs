(ns app.cljs
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [clojure.core.async]))

(defn main []
  (go
    (try (loop []
           (io/render)
           (io/write [:span "=> "])
           (let [l (io/readln)]
             (eval-str
              (empty-state)
              (str "((fn [] " l "))") ""
              {:eval js-eval
       ;:context :expr
               }(fn [res]
                  (io/writeln (pr-str (:value res)))
                  (io/write [:br])))
             (recur)))
         (catch js/Error e e))))
