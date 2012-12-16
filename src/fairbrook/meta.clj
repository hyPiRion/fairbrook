(ns fairbrook.meta)

(defn ff
  "Takes in two functions of two arguments, and returns a new function which
  applies the first function to the value of the two arguments given and applies
  the second function to their metadata. Attaches the newly generated
  metadata to the new value."
  [f meta-f]
  (fn [a b]
    (with-meta
      (f a b)
      (meta-f (meta a) (meta b)))))

(defn fm
  "Takes in a function of two arguments, and returns the same function which in
  addition merges their metadata and attaches it to the resulting value."
  [f]
  (ff f merge))

(defn duo-apply
  "Applies f with more-params and elements, and meta-f with meta-more-params and
  the metadata of the elements. Returns the result with the resulting metadata
  attached."
  {:arglists '([[f & more-params] [meta-f & meta-more-params] elements])}
  [[f & m] [mf & mm] elts]
  (with-meta
    (apply f (concat m elts))
    (apply mf (concat mm (map meta elts)))))

(defn mono-apply
  "Applies f with more-params and elements on both the data and the metadata of
  the elements. Returns the result with the resulting metadata attached."
  {:arglists '([[f & more-params] elements])}
  [args elts]
  (duo-apply args args elts))

(defn duo-reduce
  "Performs a reduction on elements with f, and their metadata with
  meta-f. Returns the result with the resulting metadata attached. If no init
  values are given, the init value will be empty maps."
  ([f meta-f elements]
     (duo-reduce f {} meta-f {} elements))
  ([f init meta-f meta-init elements]
     (with-meta
       (reduce f init elements)
       (reduce meta-f meta-init (map meta elements)))))

(defn mono-reduce
  "Performs a reduction on elements and their metadata with f. Returns the
  result with the resulting metadata attached. If no init value is given, the
  init value will be an empty map."
  ([f elements]
     (mono-reduce f {} elements))
  ([f init elements]
     (duo-reduce f init f init elements)))

(defn meta-merge
  "As merge, but will merge the metadata from each map as well. As with merge,
  if more than one of the metadata maps contains the same key, the mapping from
  the latter will be the mapping in the resulting metadata map."
  [& maps]
  (mono-reduce merge maps))

(defn meta-merge-with
  "As merge-with, but will merge the metadata from each map as well. If a key
  occurs in more than one map or metamap, the mapping(s) from the
  latter (left-to-right) will be combined with the mapping in the result by
  calling (f val-in-result val-in-latter)."
  [f & maps]
  (mono-reduce
   (fn [m1 m2]
     (merge-with f m1 m2))
   maps))
