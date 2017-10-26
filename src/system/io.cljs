(ns system.io
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [system.io :as io])
  (:require
   [reagent.core :as r]
   [cljs.core.async :as a :refer [<! timeout]]))

(defonce input-chan (a/chan))

(def default-state {:cursor-index 0
                    :input-start-index 0
                    :mode :insert
                    :out ["\u00a0"]})

(defonce state (r/atom {:cursor-index 0
                        :input-start-index 0
                        :mode :insert
                        :out ["\u00a0"]}))
;(defonce *display* out)


(defn convert-space->nbsp [s]
  (if (string? s)
    (.replace (.replace s (js/RegExp. " " "g") "\u00a0") (js/RegExp. "-" "g") "\u2011")
    (if (coll? s) (into [] (map #(convert-space->nbsp %) s)) s)))

(defn scroll-down []
  (go
    (<! (timeout 100))
    (when-let [el (js/document.getElementById "out")]
      (goog.object/set el "scrollTop"  js/Number.MAX_SAFE_INTEGER))))

(defn extract-string [hc]
  (if (string? hc)
    (.replace (.replace hc (js/RegExp. "\u00a0" "g") " ") (js/RegExp. "\u2011" "g") "-")
    (if (coll? hc)
      (reduce #(str (extract-string %1) (extract-string %2)) hc)
      "")))

(defn throw-err [e]
  (if (instance? js/Error e) (throw e) e))

(defn write-at
  ([v]
   (let [cursor-index (:cursor-index @state)
         before-items (subvec (:out @state) 0 cursor-index)
         after-items (subvec (:out @state) cursor-index)]
     (swap! state assoc
            :out (into [] (concat before-items (convert-space->nbsp [v]) after-items))
            :cursor-index (inc (:cursor-index @state))))
   (scroll-down)
   @state)
  ([& vs]
   (let [cursor-index (:cursor-index @state)
         before-items (subvec (:out @state) 0 cursor-index)
         after-items (subvec (:out @state) cursor-index)]
     (swap! state assoc
            :out (into [] (concat before-items (convert-space->nbsp vs) after-items))
            :cursor-index (+ (:cursor-index state) (count vs))))
   (scroll-down)
   @state))

(defn write [v]
  (let [out (merge (subvec (:out @state) 0 (dec (count (:out @state)))) (convert-space->nbsp v) (last (:out @state)))
        l (dec (count out))]
    (swap! state assoc
           :out out
           :cursor-index (inc (:cursor-index @state))
           :input-start-index l))
  (scroll-down))

(defn writeln [l]
  (write [:br])
  (write l))

(defn writelnn [l]
  (writeln l)
  (write [:br]))

(defn clear []
  (reset! state default-state))

(defn put! [e]
  (a/put! input-chan e))

(defn mark-input-start []
  (swap! state assoc :input-start-index (dec (count (:out @state)))))

(defn cursor-move-left [state]
  (if (> (:cursor-index state) (:input-start-index state))
    (assoc state :cursor-index (dec (:cursor-index state)))
    state))

(defn cursor-move-right [state]
  (when (< (:cursor-index state) (dec (count (:out state))))
    (assoc state :cursor-index (inc (:cursor-index state)))))

(defn pre-input [state]
  (let [i (:input-history-index state)]
    (if (> i 0)
      (assoc state
             :input-history-index (dec i)
             :input (get (:input-history state) (dec i))
             :cursor-index (count (get (:input-history state) (dec i))))
      state)))

(defn next-input [state]
  (let [i (:input-history-index state)]
    (if (< i (dec (count (:input-history state))))
      (assoc state
             :input-history-index (inc i)
             :input (get (:input-history state) (inc i))
             :cursor-index (count (get (:input-history state) (inc i))))
      state)))

(defn cursor-move-beg [state]
  (assoc state :cursor-index (:input-start-index state)))
(defn cursor-move-end+1 [state]
  (assoc state :cursor-index (count (:out state))))
(defn cursor-move-end [state]
  (let [i (dec (count (:out state)))]
    (if (> i (:input-start-index state))
      (assoc state :cursor-index (dec i)))))

(defn delete-char-pre [state]
  (let [ci (:cursor-index state)
        lo (subvec (:out state) 0 (dec ci))
        ro (subvec (:out state) ci)
        o (into [] (concat lo ro))]
    (if (> ci (:input-start-index state))
      (-> state
          (assoc :out o
                 :cursor-index (dec ci))))))

(defn delete-char [state]
  (let [ci (:cursor-index state)
        lo (subvec (:out state) 0 ci)
        ro (subvec (:out state) (inc ci))
        o (into [] (concat lo ro))]
    (when (< ci (dec (count (:out state))))
      (-> state
          (assoc :out o)))))

(defn delete-char-current [state]
  (-> state
      (cursor-move-right)
      (delete-char)))

(def shortcuts
  {:insert  [[:char #(write-at (:value %2))]
             ["ctrl+l" clear]
             ["arrowleft" cursor-move-left]
             ["arrowright" cursor-move-right]
             ["arrowup" pre-input]
             ["arrowdown" next-input]
             ["home" cursor-move-beg]
             ["end" cursor-move-end+1]
              ;["enter" execute :execute]
             ["backspace" delete-char-pre]
             ["escape" cursor-move-left :normal]
             ["ctrl+c" #(throw (js/Error. "terminate"))]]
   :normal [["h" cursor-move-left]
            ["l" cursor-move-right]
            ["k" pre-input]
            ["j" next-input]
            ["i" nil :insert]
            ["home" cursor-move-beg]
            ["end" cursor-move-end]
            ["a" cursor-move-right :insert]
            ["0" cursor-move-beg]
            ["$" cursor-move-end]
            ["x" delete-char]
             ;["enter" execute :execute]
            ["backspace" cursor-move-left]
            ["ctrl+c" #(throw (js/Error. "terminate"))]]})

(defn resolve-shortcut [state in]
  (let [kbs ((:mode state) shortcuts)]
    (loop [kbs kbs]
      (let [[kb f next-mode] (first kbs)
            capture? (cond
                       (= :* kb) (fn [] true)
                       (string? kb) #(= kb (:value %))
                       (keyword? kb) #(= kb (:type %))
                       :else kb)]
        (if (capture? in)
          (let [st (if f (f state in) state)
                st (if (nil? st) state st)
                flag (and next-mode (not (false? st)))
                st (if flag (assoc st :mode next-mode) st)]
            st)
          (if (> (count kbs) 1) (recur (rest kbs)) state))))))

(defn read []
  (go (try
        (let [in (<! input-chan)
              st (resolve-shortcut @state in)]
          (reset! state st)
          in)
        (catch js/Error e e))))

(defn readchar []
  (go-loop []
    (let [in (<! (read))
          {:keys [value]} in]
      (if (char? value) (do (write value) value) (recur)))))

(defn readln-old []
  (go-loop [l ""]
    (let [in (<! (read))
          {v :value} in]
      (if (= v "enter")
        l
        (if (instance? js/Error v) v
            (recur (if (char? v) (str l v) l)))))))

(defn readln []
  (go-loop []
    (try
      (let [in (io/<? (read))
            {v :value} in]
        (if (= v "enter")
          (let [l (extract-string (butlast (subvec (:out @state) (:input-start-index @state))))]
            (mark-input-start)
            (swap! state assoc :cursor-index (dec (count (:out @state))) :mode :insert)
            l)
          (recur)))
      (catch js/Error e e))))

(defn render
  ([]
   (render (fn []
             (let [cursor-index (:cursor-index @state)
                   mode (case (:mode @state)
                          :insert "input-cursor"
                          :normal "input-cursor-vim")
                   current-item (get-in @state [:out cursor-index])
                   before-items (subvec (:out @state) 0 cursor-index)
                   after-items (subvec (:out @state) (inc cursor-index))
                   current-item (cond
                                  (string? current-item) [:span {:class mode} current-item]
                                  (vector? current-item)
                                  (let [[tagname props & body] current-item
                                        classes (:class props)]
                                    (if (map? props)
                                      (into [tagname (assoc props :class (str classes " " mode))] body)
                                      (into (conj [tagname {:class mode}] props) body)))
                                  :else (throw (js/Error. "bad hiccup")))]
               (into [] (concat [:div#out] before-items [current-item] after-items))))))
  ([root]
   (render root {}))
  ([root opts]
   (r/render
    [root opts]
    (.getElementById js/document "app"))))

(defn init
  ([]
   (init {:keydown true
          :mousedown true
          :mouseup true
          :mousemove false
          :wheel false
          :load true
          :contextmenu true
          :time false}))
  ([opts] (let [animation-handler
                (fn animation-handler [e]
                  (a/put! input-chan {:value e :type :time})
                  (js/window.requestAnimationFrame animation-handler))
                input-handler
                (fn [e]
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
                             :else {:value s})]
                    (when in (a/put! input-chan in))))]
            (when (:keydown opts)
              (js/window.addEventListener "keydown" input-handler))
            (when (:mousedown opts)
              (js/window.addEventListener "mousedown" input-handler))
            (when (:mouseup opts)
              (js/window.addEventListener "mouseup" input-handler))
            (when (:mousemove opts)
              (js/window.addEventListener "mousemove" input-handler))
            (when (:wheel opts)
              (js/window.addEventListener "wheel" input-handler))
            (when (:load opts)
              (js/window.addEventListener "load" input-handler))
            (when (:contextmenu opts)
              (js/window.addEventListener "contextmenu" input-handler))
            (when (:time opts)
              (js/window.requestAnimationFrame animation-handler))
            (render))))

