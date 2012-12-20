(ns fairbrook.path
  (:require [fairbrook.rule :as rule]))

(defn- merge-with-path2
  "Utility function for merge-with-path"
  [f root]
  (fn [m1 m2]
    (let [merge-entry (fn [m e]
                        (let [k (key e), v (val e)]
                          (if (contains? m k)
                            (assoc m k (f (conj root k) (get m k) v))
                            (assoc m k v))))]
      (reduce merge-entry (or m1 {}) (seq m2)))))

(defn merge-from-root-fn ; Better (?) API for
  "Returns a function merging two maps together \"from root\". If a key
  collision occurs, associates the key k with (f [k] v1 v2) in the resulting
  map, where v1 and v2 are the values associated with k in m1 and m2."
  [f]
  (merge-with-path2 f []))

(defn merge-with-path-fn
  "Returns a function taking three arguments: root, m1 and m2, which will merge
  the maps m1 and m2. If a key collision occurs, associates the key k with
  (f (conj root k) v1 v2) in the resulting map, where v1 and v2 are the values
  associated with k in m1 and m2."
  [f]
  (fn [root m1 m2]
    ((merge-with-path2 f root) m1 m2)))

(defn merge-with-path
  "As merge-with, but adds the path to the function call: If a key occurs in
  more than one map, the mapping(s) will be combined by calling
  (f path val-in-result val-in-latter).

  A path is the vector of keys pointing to a value in a nested map, such
  that (get-in map path) refers to the value path is associated with."
  [f & maps]
  (reduce (merge-with-path2 f []) maps))

(defn path-merge
  "A deeper key-merge. If a key occurs in more than one map and the path is NOT
  contained within `rules`, the result is recursively merged. If the path is
  contained within `rules`, the mapping(s) from the latter will be combined with
  the mapping in the result by calling
  ((get rules path) val-in-result val-in-latter).

  A path is the vector of keys pointing to a value in a nested map, such
  that (get-in map path) refers to the value path is associated with.

  As multiple keys within maps will be recursively merged, this function will
  throw an error if `merge` cannot be applied on them."
  [rules & maps]
  (let [merge-fn (fn merge-fn [path v1 v2]
                   (if-let [rule (get rules path)]
                     (rule v1 v2)
                     ((merge-with-path2 merge-fn path) v1 v2)))]
    (apply merge-with-path merge-fn maps)))

(defn path-merge-with
  "As path-merge, but takes a default merge function `f` if the path is not
  within `rules` and a collision has occured. `f` takes three arguments: The
  path, val-in-result and val-in-latter. As such, path-merge-with is not
  recursive, like path-merge."
  [rules f & maps]
  (apply merge-with-path (rule/rule-fn rules f) maps))
