(set-env!
 :resource-paths #{"src", "resources"}
 :dependencies '[[org.clojure/clojure "1.7.0"]
                 [boot/core "2.6.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [bidi "2.0.9"]
                 [markdown-clj "0.9.89"]
                 [selmer "1.0.7"]
                 [hiccup "1.0.5"]
                 [oauth-clj "0.1.15"]
                 [environ "1.1.0"]
                 [boot-environ "1.1.0"]
                 ])

;; TEST TASKS

; Load BUILDer libraries
(require '[ontodev.builder.builder :refer :all]
         '[ontodev.builder.tasks.view]
         '[ontodev.builder.auth.view])

; Define the project
(def builder-project
  {:organization "my-org"
   :project "my-project"
   :homepage "http://github.com/my-org/my-project"
   :views [ontodev.builder.tasks.view/config
           ontodev.builder.auth.view/config]
   :auth-enabled? false})

(deftask build
  "Project-specific build task."
  []
  (println "BUILD"))
