(ns cloui.core
  (:gen-class 
    :extends javafx.application.Application
    :state root
    :init initi
    ;:methods [[render [clojure.lang.PersistentVector] void]
              ;]
    )
  (:require [clojure.core.async :refer [thread]])
  (:import (javafx.beans.value ChangeListener ObservableValue)
           (javafx.concurrent Worker$State)
           (javafx.event ActionEvent EventHandler)
           (javafx.scene Scene)
           (javafx.scene.control Button Label)
           (javafx.scene.layout StackPane)
           (javafx.stage Stage)
           (javafx.application Application)
           (javafx.scene.web WebView)))

(defn -initi []
  [[] (atom nil)]
  )

(def root (atom nil))

(defn -set-props []
  )

(defn -destruct-node [[node-name props & childs :as node]]
  (if (string? node)
    []
    (if (map? props) 
      [node-name props childs]
      [node-name nil (cons props childs)])
  ))

(defn -create-node [node]
  (let [[node-name props childs] (-destruct-node node)
        jfx-node (new node-name)
        jfx-child-nodes (for [c childs] (-create-node c))
        ]
    ))

(defn -render-to-stage [stage root]
  (let [[node-name props childs] (-destruct-node root)
        [child-name child-props child-childs] (first childs)
        cc-node (first child-childs)
        ]
    (assert (= node-name Stage) "the root node must be Stage")
    (assert (= child-name Scene) "the child of root node must be Scene")
    (let [scene (Scene. (-create-node cc-node) (:width child-props) (:height child-props))
          ]
      (. stage setScene scene)
    ))
  (let [root (StackPane.)
        btn (Button.)]

    ;; add a Button with a click handler class floating on top of the WebView
    (.setTitle stage "JavaFX app with Clojure")
    (.setText btn "Just a button")
    (.setOnAction btn
                  (proxy [EventHandler] []
                    (handle [^ActionEvent event]
                      (println "The button was clicked"))))
    (.add (.getChildren root) btn)

    ;; Set scene and show stage
    (.setScene stage (Scene. root 800 600))
    ;(.show (.stage this))
    ))
(def jfx-app nil)

(defn -render [r]
  (reset! root r)
  (when-not jfx-app
    (set! jfx-app (thread (Application/launch cloui.core (into-array String []))))
    )
  ;(Application/launch cloui.core (into-array String []))
  )

(defn -start [this ^Stage stage]
  (.show stage)
  (-render-to-stage stage @root)
  ;(reset! (.state this) stage)
  )

