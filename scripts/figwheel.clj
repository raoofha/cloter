(require '[figwheel-sidecar.repl-api :as ra :refer [start-figwheel! cljs-repl fig-status start-autobuild stop-autobuild build-once switch-to-build reset-autobuild reload-config api-help]]
         'clojure.main
         '[clojure.tools.nrepl.server :as nrepl-server]
         '[cider.nrepl :refer (cider-nrepl-handler)])

(defn cljs [] (ra/cljs-repl "dev"))

(nrepl-server/start-server :port 7888 :handler cider-nrepl-handler)
(start-figwheel!)
(cljs)
(clojure.main/main)
