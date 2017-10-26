(ns app.coffee
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [clojure.core.async]))

(defn main []
  (go-loop [prompt true]
    (when prompt (io/write [:span "> "]))
    (let [l (<! (io/readln))]
      (if (instance? js/Error l) l
          (do
            (when-not (= l "")
              (io/writelnn (js/String (js/eval (js/CoffeeScript.compile l #js {:bare true})))))
            (recur (not (= l ""))))))))
