(ns ontodev.builder.new
  "Generate project scaffolding based on a template.
  Adapted from leiningen.new, with permission of the Leiningen team."
  {:boot/export-tasks true}
  (:refer-clojure :exclude [new])
  (:require [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.util :as util]))

(def ^:const default-modules
  {:tasks {:ns    'ontodev.builder.tasks.view
           :views 'ontodev.builder.tasks.view/config}
   :auth  {:ns    'ontodev.builder.auth.view
           :views 'ontodev.builder.auth.view/config}})

(defn readme
  [name]
  (format
   "# %s

A new project using [BUILDer](http://github.com/ontodev/builder)."
   name))

(defn module-ns-str
  [module-kw]
  (if-let [module-ns (get-in default-modules [module-kw :ns])]
    (format "'[%s]" module-ns)
    (throw (RuntimeException. (format "'%s' is not an available default module" module-kw)))))

(defn module-views
  [modules]
  (into []
        (comp
         (map (fn [kw]
                (get-in default-modules [kw :views])))
         (filter identity))
        modules))

(defn build-boot
  [modules name auth-keys]
  (format
   "(set-env!
  :resource-paths #{\"src\"}
  :dependencies '[[ontodev/builder \"0.1.0-SNAPSHOT\"]])

(require '[ontodev.builder.builder :refer :all]
         %s)

(def builder-project
  {:project \"%s\"
   :views %s
   :auth-keys %s})
"
   (apply str (map module-ns-str modules))
   name
   (module-views modules)
   (or auth-keys [])))

(boot/deftask new
  "Create a new BUILDer project."
  [n name NAME str "generated project name"
   m modules MODULES edn "builder modules to include"
   a auth-keys AUTHKEYS edn "authentication api keys to include"]
  (cond
    (not (string? name))
    (throw (RuntimeException. "--name is required"))

    (.exists (io/file name))
    (throw (RuntimeException. (format "'%s' already exists" name)))

    (and modules (not (coll? modules)))
    (throw (RuntimeException. "--modules must be formatted as a collection"))

    (and auth-keys (not (coll? auth-keys)))
    (throw (RuntimeException. "--auth-keys must be formatted as a collection"))

    :else
    (do
      (.mkdir (io/file name))
      (.mkdir (io/file name "src"))
      (spit (io/file name "README.md") (readme name))
      (spit (io/file name "build.boot") (build-boot modules name auth-keys))
      (println (format "Created project directory '%s'" name)))))
