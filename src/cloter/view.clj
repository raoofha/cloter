(ns cloter.view)

(defn root []
  [:html {:width 1280 :height 720}
   [:head
    [:style "body {background-color:black;color:white;}"]]
   [:body
    [:div "Yeh"]
    [:button#p "click here"]]])

