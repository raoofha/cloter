(ns cloter.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.core :refer [html]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            ))

(defn get-filenames [dir]
  (map #(.replaceFirst (.getName %) "[.][^.]+$" "") 
               (filter #(not (.isDirectory %)) (file-seq (clojure.java.io/file dir))))
  )

(defn index []
  [:html
   [:head
    [:link {:rel "stylesheet" :href "cloter.css"}]
    ;[:script {:src "https://unpkg.com/typescript@2.5.3/lib/typescript.js"}]
    ;[:script {:src "https://rawgit.com/BuckleScript/bucklescript-playground/master/exports.js"}]
    ;[:script {:src "http://coffeescript.org/browser-compiler/coffeescript.js"}]
    ]
   [:body
    [:div#app]
    [:script {:src ".stuff/cloter.js"}]
    [:script (reduce str "\n" (map #(str "goog.require('app." % "');\n") (get-filenames "src/app")))]
    [:script "cloter.client.init();"]
    ]
   ]
  )

(defroutes app-routes
  (GET "/" [] (html (index)))
  (GET "/test" [] "Hello test")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 3000))]
    (run-jetty app {:port port :join? false})))
