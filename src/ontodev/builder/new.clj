(ns ontodev.builder.new
  "Generate project scaffolding based on a template.
  Adapted from leiningen.new, with permission of the Leiningen team."
  {:boot/export-tasks true}
  (:refer-clojure :exclude [new])
  (:require [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.util :as util]))

(defn readme
  [name]
  (format
   "# %s

A new project using [BUILDer](http://github.com/ontodev/builder)."
   name))

(defn build-boot
  [name]
  (format
   "(set-env!
  :resource-paths #{\"src\"}
  :dependencies '[[ontodev/builder \"0.1.0-SNAPSHOT\"]])

(require '[ontodev.builder.builder :refer :all])

(def builder-project
  {:project \"%s\"})
" name))

(boot/deftask new
  "Create a new BUILDer project."
  [n name NAME str "generated project name"]
  (cond
    (not (string? name))
    (throw (RuntimeException. "--name is required"))

    (.exists (io/file name))
    (throw (RuntimeException. (format "'%s' already exists" name)))

    :else
    (do
      (.mkdir (io/file name))
      (.mkdir (io/file name "src"))
      (spit (io/file name "README.md") (readme name))
      (spit (io/file name "build.boot") (build-boot name))
      (println (format "Created project directory '%s'" name)))))
