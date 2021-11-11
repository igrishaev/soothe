(ns soothe.core
  (:refer-clojure :exclude [def defmulti])
  (:require
   [soothe.en :as en]
   [clojure.spec.alpha :as s]))


(alias 'sth 'soothe.core)


(defonce ^:private
  -registry (atom nil))


(defn- resolve-message [the-key problem]
  (when-let [result
             (-> -registry deref (get the-key))]
    (if (fn? result)
      (result problem)
      result)))


(defn- missing-key?
  [{:keys [pred]}]
  (when (seq? pred)
    (let [[sym-fn vec-% form-contains] pred]
      (when (= [sym-fn vec-%] '[clojure.core/fn [%]])
        (when (seq? form-contains)
          (let [[sym-contains % kw-key] form-contains]
            (when (= [sym-contains %] '[clojure.core/contains? %])
              kw-key)))))))


(defn- conformer-pred? [pred]
  (when (seq? pred)
    (let [[sym-conformer pred-inner] pred]
      (when (= sym-conformer `s/conformer)
        pred-inner))))


(defn- problem->error
  [{:as problem
    :keys [path pred val via in]}]

  (let [message
        (or

         ;; Resolve by a symbol.
         (when (qualified-symbol? pred)
           (resolve-message pred problem))

         ;; Resovle by a keyword.
         (when-let [spec (peek via)]
           (resolve-message spec problem))

         ;; Special case: s/keys misses a key.
         (when-let [kw-key
                    (missing-key? problem)]
           (let [problem*
                 (assoc problem :key kw-key)]
             (resolve-message ::missing-key problem*)))

         ;; Special case: unwrap s/conformer.
         (when-let [pred-inner
                    (conformer-pred? pred)]
           (when (qualified-symbol? pred-inner)
             (resolve-message pred-inner problem)))

         ;; Not found.
         (resolve-message ::default problem))]

    {:message message
     :path path
     :val val}))


(defn- map-explain->errors [map-explain]
  (let [{::s/keys [problems]}
        map-explain]
    {:problems
     (mapv problem->error problems)}))


;;
;; Public
;;

(defn def
  [kw-spec|sym-pred fn|message]
  (swap! -registry assoc kw-spec|sym-pred fn|message)
  nil)


(defn defmulti
  [key->messages]
  (swap! -registry merge key->messages)
  nil)


(defn undef [kw-spec|sym-pred]
  (swap! -registry dissoc kw-spec|sym-pred)
  nil)


(defn undef-all []
  (reset! -registry nil)
  nil)


(defn explain-data [spec value]
  (when-let [explain
             (s/explain-data spec value)]
    (map-explain->errors explain)))


(defn explain [spec value]
  (when-let [{:keys [problems]}
             (explain-data spec value)]

    (println "Problems:")
    (println)

    (doseq [{:keys [val
                    path
                    message]} problems]

      (println "-" message)
      (println " " "path:" path)
      (println " " "value:" val)
      (println))))


(defn explain-str [spec value]
  (with-out-str
    (explain spec value)))


;;
;; EN defaults
;;

(sth/defmulti en/presets)
