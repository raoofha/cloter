(ns app.sh
  (:require-macros
   ;[system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.js :refer [empty-state eval-str js-eval *eval-fn*]]
            ;[cljs.core.async :refer [put!]]
            [system.io :as io]))

(defn main []
  (system.io/init)
  (go-loop []
    (io/prompt [:span.prompt "➤ "])
    (let [l (<! (io/readln {:not-empty false}))
          app (goog.object/getValueByKeys js/window "app" l "main")
          ]
      (io/write :br)
      (when app (<! (app)) (io/write :br))
      (recur))))
