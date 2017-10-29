(ns system.io
  #_(:require [clojure.core.async :as async :refer [>! <! go chan]]
              [system.io :as io]))

(defmacro <? [ch]
  `(throw-err (cljs.core.async/<! ~ch)))

(defmacro defmain [args & body]
  `(defn ~'main ~args
     (cljs.core.async.macros/go (try ~@body (catch js/Error e e)))))
