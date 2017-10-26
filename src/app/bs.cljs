(ns app.bs
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
              (io/writelnn (js/String (js/eval (goog.object/get (js/JSON.parse (js/ocaml.compile l)) "js_code")))))
            (recur (not (= l ""))))))))
