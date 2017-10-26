(ns app.console
  (:require-macros
   ;[system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.js :refer [empty-state eval-str js-eval *eval-fn*]]
            ;[cljs.core.async :refer [put!]]
            [system.io :as io]
            [clojure.core.async]))

#_(defn main []
    (go-loop [st state]
      (let [in (io/read)
            nst (next-state st in)
            ef (:effect nst)]
      ;(when (not= nst st) (io/render (:render nst) nst))
        (when ef (ef #(io/put! %)))
        (when (= (:name nst) :execute)
          (let [;n-s (symbol (str "app." (:input nst)))
              ;_ (require n-s)
                fn- (goog.object/getValueByKeys js/window "app" (:input nst) "main")
                res (when fn- (<! (fn-)))]
            (when (instance? js/Error res)
              (io/put! {:type :load})
              (recur (-> nst clear-input (rename :insert))))))
        (recur nst))))

(defn main []
  (go-loop []
    (io/prompt [:span.prompt "➤ "])
    (let [l (<! (io/readln))
          app (goog.object/getValueByKeys js/window "app" l "main")]
      (io/write [:br])
      (when app (<! (app)) (io/write [:br]))
      (recur))))
