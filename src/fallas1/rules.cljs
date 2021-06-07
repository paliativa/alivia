(ns fallas1.rules
  (:require [clara.rules
             :refer [insert fire-rules]
             :refer-macros [defsession defrule]]))

(defrecord Zone [value])
(defrecord Intensity [value])
(defrecord Characteristic [value])
(defrecord Duration [value])
(defrecord Constancy [value])
(defrecord Stimulus [value])

(defrule is-important
  "Find important support requests."
  [Zone (= :yes value)]
  =>
  (println "High support requested!"))

(defrule notify-client-rep
  "Find the client representative and request support."
  [Zone (= ?zone value)]
  [Intensity (= ?zone value) (= ?name name)]
  =>
  (println "Notify" ?name "that"
           ?zone "has a new support request!"))

(defsession session 'fallas1.rules)
