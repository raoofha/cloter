(ns app.console
  (:require-macros
   [system.io :as io]
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.js :refer [empty-state eval-str js-eval *eval-fn*]]
            ;[cljs.core.async :refer [put!]]
            [system.io :as io]
            [clojure.core.async]))

;(set! *eval-fn* js-eval)
;(defonce cljs-repl-state (empty-state))

(defn convert-space->nbsp [s]
  (if (string? s)
    (.replace (.replace s (js/RegExp. " " "g") "\u00a0") (js/RegExp. "-" "g") "\u2011")
    (if (coll? s) (into [] (map #(convert-space->nbsp %) s)) s)))

(defn write [state & vs]
  (assoc state :history (into (:history state) (convert-space->nbsp vs))))

(defn writeln [state in]
  (write state in [:br]))

(defn clear [state] (assoc state :history []))

(defn prompt [state] (write state [:span.prompt "➤ "]))

(defn write-input [state in]
  (let [li (.substring (:input state) 0 (:cursor-index state))
        ri (.substring (:input state) (:cursor-index state))
        i (str li (:value in) ri)]
    (-> state
        (assoc :input i :cursor-index (inc (:cursor-index state)))
        (assoc-in [:input-history (dec (count (:input-history state)))] i))))

(defn clear-input [state]
  (assoc state :input "" :cursor-index 0))

(defn cursor-move-left [state]
  (if (> (:cursor-index state) 0)
    (assoc state :cursor-index (dec (:cursor-index state)))
    state))

(defn cursor-move-right [state]
  (if (< (:cursor-index state) (.-length (:input state)))
    (assoc state :cursor-index (inc (:cursor-index state)))
    state))

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
  (assoc state :cursor-index 0))
(defn cursor-move-end+1 [state]
  (assoc state :cursor-index (.-length (:input state))))
(defn cursor-move-end [state]
  (let [i (dec (.-length (:input state)))]
    (if (> i -1)
      (assoc state :cursor-index (dec (.-length (:input state))))
      state)))

(defn delete-char-pre [state]
  (let [ci (:cursor-index state)
        li (.substring (:input state) 0 (dec ci))
        ri (.substring (:input state) ci)
        i (str li ri)]
    (-> state
        (assoc :input i
               :cursor-index (if (> ci 0) (dec (:cursor-index state)) ci))
        (assoc-in [:input-history (dec (count (:input-history state)))] i))))

(defn delete-char [state]
  (let [ci (:cursor-index state)
        li (.substring (:input state) 0 ci)
        ri (.substring (:input state) (inc ci))
        i (str li ri)]
    (-> state
        (assoc :input i
               ;:cursor-index (if (< ci (count i)) (inc (:cursor-index state)) ci)
)
        (assoc-in [:input-history (dec (count (:input-history state)))] i))))

(defn delete-char-current [state]
  (-> state
      (cursor-move-right)
      (delete-char)))

(defn execute [state]
  (let [cmd (:input state)
        cmd-parts (filter #(not= % "") (clojure.string/split cmd #" "))
        cmd-name (first cmd-parts)]
    (if (not= cmd "")
      [(-> state (writeln cmd))
       (fn [cb]
         (goog.object/set (-> (js/document.getElementsByClassName "console") (aget 0))  "scrollTop"  js/Number.MAX_SAFE_INTEGER)
         (eval-str (empty-state) (str "((fn [] " cmd "))") "" {:eval js-eval
                                                         ;:context :return
                                                               }(fn [res] (cb {:value res :type :cljs-output}))))]
      false)))

(defn terminate [state]
  state)

;(defn put-back [state in]
  ;[state #(% in)]
  ;)

(defn console-log [state in]
  (write state [:span [:span (pr-str in)] [:br]]))

(defn cljs-pprint [state in]
  (let [p (if (:error in) (:error in) (:value in))]
    (write state [:span [:span (pr-str p)] [:br]])))

(defn rename [state new-name]
  (assoc state :name new-name))

(defn render [state]
  (into (into [:div.console #_[:div (str (:time state))]] (:history state))
        (let [s (convert-space->nbsp (:input state))
              sl (.substring s 0 (:cursor-index state))
              c (get s (:cursor-index state))
              sc (if c c "\u00a0")
              sr (.substring s (inc (:cursor-index state)))]
          (if (= :insert (:name state))
            [sl [:span.input-cursor sc] sr]
            (if (= :normal (:name state))
              [sl [:span.input-cursor-vim sc] sr]
              [s])))))

(def state
  {:name :insert
   :input ""
   :last-input ""
   :cursor-index 0
   :input-history [""]
   :input-history-index 0
   :history []
   :render render
   :state-graph
   {:insert  [[:char write-input]
              [:load prompt]
              ["ctrl+l" #(do (-> % (clear) (prompt)))]
              ["arrowleft" cursor-move-left]
              ["arrowright" cursor-move-right]
              ["arrowup" pre-input]
              ["arrowdown" next-input]
              ["home" cursor-move-beg]
              ["end" cursor-move-end+1]
              ["enter" execute :execute]
              ["backspace" delete-char-pre]
              ["escape" cursor-move-left :normal]
              ;["time" #(assoc %1 :time (:time %2))]
]
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
             ["enter" execute :execute]
             ["backspace" cursor-move-left]]
    :execute [["ctrl+c" terminate :insert]
              ;[:cljs-output #(do (-> % clear-input (cljs-pprint (:value %2)) prompt)) :insert]
              ;[:* put-back]
]}})

(defn next-state [state in]
  (let [kbs (get-in state [:state-graph (:name state)])]
    (loop [kbs kbs]
      (let [[kb f nstate-name] (first kbs)
            capture? (cond
                       (= :* kb) (fn [] true)
                       (string? kb) #(= kb (:value %))
                       (keyword? kb) #(= kb (:type %))
                       :else kb)]
        (if (capture? in)
          (let [st (if f (f state in) state)
                st-not-false (not (false? st))
                st (cond
                     (vector? st) (assoc (get st 0) :effect (get st 1))
                     (false? st) state
                     :else st)
                st (if (and nstate-name st-not-false) (assoc st :name nstate-name) st)]
            st)
          (if (> (count kbs) 1) (recur (rest kbs)) (dissoc state :effect)))))))

(defn main []
  ;(set! system.io/*display* (:render state))
  (go-loop [st state]
    (let [in (io/read)
          nst (next-state st in)
          ef (:effect nst)]
      (when (not= nst st) (io/render nst))
      (when ef (ef #(io/put! %)))
      (when (= (:name nst) :execute)
        (let [;n-s (symbol (str "app." (:input nst)))
              ;_ (require n-s)
              res (<! ((goog.object/getValueByKeys js/window "app" (:input nst) "main")))]
          (when (instance? js/Error res)
            (io/put! {:type :load})
            (recur (-> nst clear-input (rename :insert))))))
      (recur nst))))
