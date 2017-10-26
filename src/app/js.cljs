(ns app.js
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [clojure.core.async]))

#_(defn main []
  (io/render)
  (go-loop []
           (io/write [:span "> "])
           (let [l (io/readln)]
             (io/writelnn (js/eval l))
             (recur))))

(defn main []
  (go-loop [prompt true]
    (when prompt (io/write [:span "> "]))
    ;(io/mark-input-start)
    (let [l (<! (io/readln))]
      (if (instance? js/Error l) l
          (do
            (when-not (= l "")
              (io/writelnn (js/JSON.stringify (js/eval l))))
            (recur (not (= l ""))))))))
