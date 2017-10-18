(ns cloui.core
  (:gen-class 
    :extends javafx.application.Application)
  (:require [clojure.core.async :refer [thread]]
            [hiccup.core :refer [html]])
  (:import (javafx.beans.value ChangeListener ObservableValue)
           (javafx.concurrent Worker$State)
           (javafx.event ActionEvent EventHandler)
           (javafx.scene Scene)
           (javafx.scene.control Button Label)
           (javafx.scene.layout StackPane)
           (javafx.stage Stage)
           (javafx.application Application)
           (javafx.scene.web WebView)
           (javafx.application Platform)))

(def state (atom nil))
(defn -render [root]
    (.loadContent (:browser @state) (html (root)))
    )
(defn -run [root]
  ;(Platform/setImplicitExit false)
  (swap! state assoc :root root)
  (thread (Application/launch cloui.core (into-array String [])))
  ;(Application/launch cloui.core (into-array String []))
  )

(defn -start [this ^Stage stage]
  (let [stack-pane (StackPane.)
        webview (WebView.)
        browser (.getEngine webview)
        ]
    (swap! state assoc :webview webview)
    (swap! state assoc :browser browser)
    (.setTitle stage "Cloter")
    (.add (.getChildren stack-pane) webview)
    (.setJavaScriptEnabled browser false)
    (-render (:root @state))

    (.setScene stage (Scene. stack-pane 800 600))
    (.show stage)
  ))
