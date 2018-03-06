(ns system.io
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [system.io :as io])
  (:require
   [reagent.core :as r]
   [cljs.core.async :as a :refer [<! timeout]]))

(declare default-state state write)

(defn dispatch [e]
  (let [e (cond
            (keyword? e) {:type e}
            (string? e) {:value e}
            :else e)]
    (a/put! (:input-chan @state) e)))

(defn input-handler [e]
  (when-not (or (and (.-ctrlKey e) (or (= (.-key e) "r")
                                       (= (.-key e) "R")
                                       (= (.-key e) "I")
                                       (= (.-key e) "J")
                                       (= (.-key e) "v")
                                       (= (.-key e) "=")
                                       (= (.-key e) "-")))
                (and (.-shiftKey e) (= (.-key e) "Insert"))
                (= (.-key e) "F12"))
    (.preventDefault e))
  (let [t (case (.-type e)
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
             (= (.-type e) "focus") {:type :focus}
             (= (.-type e) "blur") {:type :blur}
             (nil? s) nil
             (and (= (.-type e) "keydown") (nil? (.-key e))) (if  (= 1 (.. e -detail -length))
                                                               {:value (. e -detail) :type :char}
                                                               {:value (.. e -detail toLowerCase)})
             :else {:value s})]
    (when in (dispatch in))))

(defn animation-handler [e]
  (when (:time (last (:events-opts @state)))
    (dispatch {:value e :type :time})
    (js/window.requestAnimationFrame animation-handler)))

(defn remove-events [opts]
  (. (:target opts) removeEventListener "keydown" input-handler)
  (. (:target opts) removeEventListener "mousedown" input-handler)
  (. (:target opts) removeEventListener "mouseup" input-handler)
  (. (:target opts) removeEventListener "mousemove" input-handler)
  (. (:target opts) removeEventListener "wheel" input-handler)
  (. (:target opts) removeEventListener "contextmenu" input-handler)
  (. (:target opts) removeEventListener "blur" input-handler)
  (. (:target opts) removeEventListener "focus" input-handler)
  (js/window.removeEventListener "load" input-handler)
  (js/window.cancelAnimationFrame (:time opts)))

(defn register-events [opts]
  (if (or (:fullscreen opts) (= (:root opts) (:root @state)))
    (r/render [(:root opts) (:root-opts opts)] (:root-target opts))
    (write [(:root opts) (:root-opts opts)]))
  (when (:keydown opts) (. (:target opts) addEventListener "keydown" input-handler))
  (when (:mousedown opts) (. (:target opts) addEventListener "mousedown" input-handler))
  (when (:mouseup opts) (. (:target opts) addEventListener "mouseup" input-handler))
  (when (:mousemove opts) (. (:target opts) addEventListener "mousemove" input-handler))
  (when (:wheel opts) (. (:target opts) addEventListener "wheel" input-handler))
  (when (:contextmenu opts) (. (:target opts) addEventListener "contextmenu" input-handler))
  (when (:blur opts) (. (:target opts) addEventListener "blur" input-handler))
  (when (:focus opts) (. (:target opts) addEventListener "focus" input-handler))
  (when (:load opts) (js/window.addEventListener "load" input-handler))
  (assoc opts :time (when (:time opts) (js/window.requestAnimationFrame animation-handler))))

(defn convert-space->nbsp [s]
  (if (string? s)
    (.replace (.replace s (js/RegExp. " " "g") "\u00a0") (js/RegExp. "-" "g") "\u2011")
    (if (coll? s) (into [] (map #(convert-space->nbsp %) s)) s)))

(defn scroll-down []
  (r/after-render #(when-let [el (js/document.getElementById "out")]
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
  (let [v (cond
            (keyword? v) [v]
            (fn? v) [v]
            :else v)
        out (merge (subvec (:out @state) 0 (dec (count (:out @state)))) (convert-space->nbsp v) (last (:out @state)))
        l (dec (count out))]
    (swap! state assoc
           :out out
           :cursor-index (inc (:cursor-index @state))
           :input-start-index l))
  (scroll-down))

(defn writeln [l]
  (write :br)
  (write l))

(defn writelnn [l]
  (writeln l)
  (write :br))

(defn prompt [p]
  (swap! state assoc :prompt p)
  (write p))

(defn clear []
  (let [p (:prompt @state)]
    (swap! state assoc
           :out ["\u00a0"]
           :cursor-index 0
           :input-start-index 0)
    (when p (write p))
    @state))

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
  (assoc state :cursor-index (dec (count (:out state)))))
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

(defn close []
  (when (> (count (:input-chans @state)) 0)
    (swap! state assoc :input-chan (last (:input-chans @state)))
    (swap! state update :input-chans #(into [] (rest %)))
    (remove-events (last (:events-opts @state)))
    (swap! state update :events-opts #(into [] (butlast %)))
    (register-events (last (:events-opts @state)))
    (reset! state (cursor-move-end+1 @state)))
  (throw (js/Error. "close")))

(defn new-app-id []
  (swap! state update :app-counter inc)
  (:app-counter @state))

(defn resolve-event [kbs stat in]
  (when (and kbs (= (:app-id in) (:app-id stat)))
    (loop [kbs kbs]
      (let [[kb f next-mode] (first kbs)
            capture? (cond
                       (= :* kb) (fn [] true)
                       (string? kb) #(= kb (:value %))
                       (keyword? kb) #(= kb (:type %))
                       :else kb)]
        (if (capture? in)
          (let [st (if f (f stat in) stat)
                st (if (nil? st) stat st)
                flag (and next-mode (not (false? st)))
                st (if flag (assoc st :mode next-mode) st)]
            st)
          (if (> (count kbs) 1) (recur (rest kbs)) nil))))))

(defn resolve-event-from-mode [state in]
  (let [mode (:mode state)
        evs (:events state)
        st (resolve-event (:* evs) state in)]
    (if st st
        (when mode (resolve-event (mode evs) state in)))))

(defn read []
  (go (try
        (let [in (<! (:input-chan @state))
              st (resolve-event-from-mode @state in)]
          (when st (reset! state st))
          in)
        (catch js/Error e e))))

(defn readchar []
  (go-loop []
    (let [in (<! (read))
          {:keys [value]} in]
      (if (char? value) (do (write value) value) (recur)))))

(defn readln
  ([] (readln {:not-empty true}))
  ([opts]
   (swap! state assoc :mode :insert)
   (go-loop []
     (try
       (let [in (io/<? (read))
             {v :value} in]
         (if (= v "enter")
           (let [l (extract-string (butlast (subvec (:out @state) (:input-start-index @state))))]
             (when (and (= l "") (:not-empty opts)) (recur))
             (mark-input-start)
             ;(swap! state assoc :cursor-index (dec (count (:out @state))) :mode :insert)
             (swap! state assoc :cursor-index (dec (count (:out @state))) :mode nil)
             l)
           (recur)))
       (catch js/Error e e)))))

(defn init
  ([] (init {}))
  ([opts]
   (let [root (:root opts)
         opts (merge {:keydown true
                      :mousedown false
                      :mouseup false
                      :mousemove false
                      :wheel false
                      :load false
                      :contextmenu true
                      :time false
                      :blur true
                      :focus true
                      :target js/window
                      :root-target (.getElementById js/document "root")
                      :root (:root @state)} opts)]
     (when (:input-chan @state) (swap! state update :input-chans conj (:input-chan @state)))
     (swap! state assoc :input-chan (a/chan))
     (swap! state update :events-opts conj (register-events opts)))))

(def default-state {:cursor-index 0
                    :input-start-index 0
                    :mode :insert
                    :focus true
                    :out ["\u00a0"]
                    :input-chans []
                    :events-opts []
                    :events
                    {:* [["ctrl+c" close]
                         [:blur #(assoc % :focus false)]
                         [:focus #(assoc % :focus true)]
                         ["ctrl+l" clear]]
                     :insert  [[:char #(write-at (:value %2))]
                               ["arrowleft" cursor-move-left]
                               ["arrowright" cursor-move-right]
                               ["arrowup" pre-input]
                               ["arrowdown" next-input]
                               ["home" cursor-move-beg]
                               ["end" cursor-move-end+1]
                               ["backspace" delete-char-pre]
                               ["escape" cursor-move-left :normal]]
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
                              ["backspace" cursor-move-left]]}
                    :root (fn []
                            (let [cursor-index (:cursor-index @state)
                                  mode (case (:mode @state)
                                         :insert "cursor-insert"
                                         :normal "cursor-normal"
                                         nil nil)
                                  mode (when (:focus @state) mode)
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
                              (into [] (concat [:div#out] before-items [current-item] after-items))))})

(defonce state (r/atom default-state))
