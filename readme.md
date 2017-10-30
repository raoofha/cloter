# cloter

### simple clojurescript repl
```clj
(ns app.cljs
  (:require [system.io :as io :refer-macros [<? defmain]]
            [cljs.js :refer [empty-state eval-str js-eval]]))

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
```

### date command
```clj
(ns app.date
  (:require [system.io :as io :refer-macros [<? defmain]]))

(defmain []
  (io/write (js/String (js/Date.))))
```
