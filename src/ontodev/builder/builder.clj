(ns ontodev.builder.builder
  (:require [boot.core :as boot]
            [ontodev.builder.server :refer [serve!]]))

(boot/deftask builder
  "Run a BUILDer server."
  []
  (let [config (var-get (resolve 'boot.user/builder-project))]
    (when-not config
      (throw (RuntimeException. "builder-project must be defined")))
    (serve! config)))
