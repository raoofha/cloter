(ns system.io
  (:require [clojure.core.async :as async :refer [>! <! go chan]]
            [system.io :as io]))

(defmacro read []
  ;'(<! system.io/input-chan)
  '(let [in (<! system.io/input-chan)]
     (when (= (:value in) "ctrl+c") (throw (js/Error. "terminate")))
     in
     )
  )

(defmacro readchar []
  '(loop []
     (let [in (system.io/read)
           {:keys [value]} in]
       (if (char? value) (do (system.io/write value) value) (recur)))))

(defmacro readln []
  '(loop [l ""]
     (let [in (system.io/read)
           {:keys [value]} in]
       (if (= value "enter")
         l
         (recur (if (char? value) (do (system.io/write value) (str l value)) l))))))

#_(defmacro readln []
  `(loop [~'l ""]
     (let [~'in (read)
           {:keys [~'value]} ~'in]
       (if (= ~'value "enter")
         ~'l
         (recur (if (char? ~'value) (do (write ~'value) (str ~'l ~'value)) ~'l))))))

