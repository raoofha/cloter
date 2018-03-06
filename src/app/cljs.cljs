(ns app.cljs
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [system.io :as io :refer-macros [<? defmain]]
            [cljs.js :refer [empty-state eval-str js-eval]]
            [cljs.core.async :as a :refer [<!]]))

(defmain []
  (system.io/init)
  (loop []
    (io/prompt "=> ")
    (let [l (<? (io/readln))
          c (a/chan)
          _ (binding [*print-fn* io/writeln
                      *print-err-fn* io/writeln]
              (eval-str
               (empty-state)
               l ""
               {:context :expr
                :eval (fn [{:keys [source]}]
                        (if js/chrome.devtools
                          (js/chrome.devtools.inspectedWindow.eval
                           source
                           (fn [res err]
                             (if res
                               (a/put! c (pr-str res))
                               (a/put! c (pr-str err)))))
                          (a/put! c (pr-str (js/eval source)))))}
               identity))
          r (<! c)]
      (io/writelnn r)
      (recur))))
