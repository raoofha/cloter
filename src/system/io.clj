(ns system.io
  #_(:require [clojure.core.async :as async :refer [>! <! go chan]]
              [system.io :as io]))

(defmacro <? [ch]
  `(throw-err (cljs.core.async/<! ~ch)))

(defmacro defmain [args & body]
  `(defn ~'main ~args
     (cljs.core.async.macros/go (try ~@body (catch js/Error e e)))))

(defmacro wait [state]
  `(loop []
     (let [st# (system.io/resolve-event (:events @~state) @~state (system.io/<? (system.io/read)))]
       (when st# (reset! ~state st#))
       (recur))))

#_(defmacro wait [state]
  `(loop []
     (let [close# ["ctrl+c" system.io/close]
           evs# (:events @~state)
           evs# (if (vector? evs#) {:* (cons close# evs#)} (update evs# :* cons close#))
           st# (system.io/resolve-event-from-mode (assoc @~state :events evs#) (system.io/<? (system.io/read)))]
       (when st# (reset! ~state st#))
       (recur))))

#_(defmacro dispatch [e]
  `(let [app-id# (:app-id (last (:events-opts @system.io/state)))]
     #(cljs.core.async/put! (:input-chan @system.io/state) (assoc ~e :app-id app-id#))))
