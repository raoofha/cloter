(ns app.cljs
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [clojure.core.async :refer [<!]]))

#_(defn main []
    (go-loop [prompt true]
      (when prompt (io/write [:span "=> "]))
      (let [l (<! (io/readln))]
        (when-not (= l "")
          (eval-str
           (empty-state)
           l ""
           {:eval js-eval :context :expr}
           (fn [res]
             (io/writelnn (pr-str (:value res))))))
        (recur (not (= l ""))))))

#_(defn main []
    (go-loop [prompt true]
      (when prompt (io/write [:span "=> "]))
      (let [l (<! (io/readln))]
        (if (instance? js/Error l) l
            (do
              (when-not (= l "")
                (eval-str
                 (empty-state)
                 l ""
                 {:eval js-eval :context :expr}
                 (fn [res]
                   (io/writelnn (pr-str (:value res))))))
              (recur (not (= l ""))))))))

(defn main []
  (go-loop [prompt true]
    (when prompt (io/write [:span "=> "]))
    (let [l (io/<? (io/readln))]
      (do
        (when-not (= l "")
          (eval-str
           (empty-state)
           l ""
           {:eval js-eval :context :expr}
           (fn [res]
             (io/writelnn (pr-str (:value res))))))
        (recur (not (= l "")))))))
