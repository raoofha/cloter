(ns system.io
  (:require
   [reagent.core :as r]
   [cljs.core.async :as a]))

(defonce input-chan (a/chan))

(defonce init-input-handler
  (do
    (defn animation-handler [e]
      (a/put! input-chan {:value e :type :time})
      (js/window.requestAnimationFrame animation-handler))
    (defn input-handler [e]
      (if goog.DEBUG
        (when (not (and (.-ctrlKey e) (or (= (.-key e) "r") (= (.-key e) "R")))) (.preventDefault e))
        (.preventDefault e))
      (let [;mouse-updown
            t (case (.-type e)
                ("keyup" "mouseup") ":up"
                "mousemove" ":move"
                nil)
            s (clojure.string/join
               "+"
               (filter #(not (nil? %)) [(when (.-ctrlKey e) "ctrl")
                                        (when (.-shiftKey e) (if (.-key e) (when (> (.-length (.-key e)) 1) "shift") "shift"))
                                        (when (.-altKey e) "alt")
                                        (case (.-key e)
                                          ("Control" "Shift" "Alt") nil
                                          nil (case (.-buttons e)
                                                1 (str "leftmouse" t)
                                                2 (str "rightmouse" t)
                                                4 (str "middlemouse" t)
                                                0 (case (.-type e)
                                                    "mousemove" "mousemove"
                                                    "mouseup" (case (.-button e)
                                                                0 "leftmouse:up"
                                                                1 "middlemouse:up"
                                                                2 "rightmouse:up"
                                                                nil)
                                                    "wheel" (if (> (.-deltaY e) 0) "scrolldown" "scrollup")
                                                    nil)

                                                nil)
                                          (if (= (.-length (.-key e)) 1)
                                            (.-key e) (.toLowerCase (.-key e))))]))
            in (cond 
                 (= 1 (.-length s)) {:value s :type :char}
                 (= (.-type e) "load") {:type :load}
                 (nil? s) nil
                 :else {:value s}
                 )]
        (when in (a/put! input-chan in))))
    (js/window.addEventListener "keydown" input-handler)
    (js/window.addEventListener "mousedown" input-handler)
    (js/window.addEventListener "mouseup" input-handler)
    (js/window.addEventListener "mousemove" input-handler)
    (js/window.addEventListener "wheel" input-handler)
    (js/window.addEventListener "load" input-handler)
    (js/window.addEventListener "contextmenu" input-handler)
    ;(js/window.requestAnimationFrame animation-handler)
    ""))

(defonce out (r/atom [:div]))
(defonce *display* out)

(defn write [& vs]
  (swap! out into (into [] vs)))
(defn writeln [l]
  ;(swap! out into [[:br] l])
  (write [:br])
  (write l))

(defn clear []
  (reset! out []))

(defn put! [e]
  (a/put! input-chan e))

(defn render
  ([]
   (r/render
    [#(do @*display*)]
    (.getElementById js/document "app")))
  ([state]
   (r/render
    [(:render state) state]
    (.getElementById js/document "app"))))
