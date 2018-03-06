(ns app.js
  (:require [system.io :as io :refer-macros [<? defmain]]
            [cljs.core.async :as a :refer [<!]]))

(defmain []
  (system.io/init)
  (loop []
    (io/prompt "> ")
    (let [l (<? (io/readln))
          l (str "(()=> { return " l "})();")
          c (a/chan)
          _ (if js/chrome.devtools
              (js/chrome.devtools.inspectedWindow.eval
               l
               (fn [res err]
                 (if res
                   (a/put! c (js/JSON.stringify res))
                   (a/put! c (js/JSON.stringify err)))))
              (a/put! c (js/JSON.stringify (js/eval l))))
          r (<! c)]
      (io/writelnn r)
      (recur))))
