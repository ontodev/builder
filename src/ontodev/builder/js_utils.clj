(ns ontodev.builder.js-utils)

;; todo.. clean this up and put in an actual js file. Easier to deal with in clojure land for the time being, though
;; because javascript

(defn- ajax-req
  [req-type]
  (fn [{:keys [url data on-success]}]
    (str "$.ajax({type: \"" req-type "\",
                url: '" url "',"
         (when data (str "data: " data ","))
         "success: " (or on-success "null") "});")))

(defn post
  [params]
  ((ajax-req "POST") params))

(defn delete
  [params]
  ((ajax-req "DELETE") params))

(def reload "location.reload();")