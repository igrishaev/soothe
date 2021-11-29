(ns soothe.core
  (:refer-clojure :exclude [def])
  (:require

   #?(:clj  [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])

   [soothe.en :as en]))


(defonce ^:private
  -registry (atom nil))


(defn- resolve-message [the-key problem]
  (when-let [result
             (-> -registry deref (get the-key))]
    (if (fn? result)
      (result problem)
      result)))


(s/def ::missing-key
  (s/cat :_ '#{clojure.core/fn cljs.core/fn}
         :_ '#{[%]}
         :contains (s/spec (s/cat :_ '#{clojure.core/contains? cljs.core/contains?}
                                  :_ '#{%}
                                  :key keyword?))))


(defn- spec-parse [spec form result-path]
  (let [parsed
        (s/conform spec form)]
    (when-not (s/invalid? parsed)
      (get-in parsed result-path))))


(defn- missing-key?
  [{:keys [pred]}]
  (spec-parse ::missing-key pred [:contains :key]))


(defn- min-count?
  [{:keys [pred]}]
  (spec-parse ::min-count pred [:min-count]))


(defn- conformer-pred? [pred]
  (when (seq? pred)
    (let [[sym-conformer pred-inner] pred]
      (when (= sym-conformer `s/conformer)
        pred-inner))))


(defn- resolve-by-symbol [sym problem]
  (if (qualified-symbol? sym)
    (resolve-message sym problem)))


(defn- problem->error
  [{:as problem
    :keys [path pred val via in]}]

  (let [message
        (or

         ;; Resolve by a symbol.
         (when (symbol? pred)
           (resolve-by-symbol pred problem))

         ;; Special case: s/keys misses a key.
         (when-let [kw-key
                    (missing-key? problem)]
           (let [problem*
                 (assoc problem :key kw-key)]
             (resolve-message ::missing-key problem*)))

         ;; Resovle by spec in a reverse order.
         (some identity
               (for [spec (reverse via)]
                 (resolve-message spec problem)))

         ;; Special case: unwrap s/conformer.
         (when-let [pred-inner
                    (conformer-pred? pred)]
           (when (symbol? pred-inner)
             (resolve-by-symbol pred-inner problem)))

         ;; Not found.
         (resolve-message ::default problem))]

    {:message message
     :path in
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
  "
  Define a message for a given spec or a predicate.
  Arguments:
  - either a keyword (spec) or a qualified symbol (for a predicate);
  - either a string or 1-arg function that takes a problem map and returns a message.
  "
  [kw-spec|sym-pred fn|message]
  (swap! -registry assoc kw-spec|sym-pred fn|message)
  nil)


(defn def-many
  "
  Define multiple messages at once.
  Takes a map of sym/keyword => message/function values.
  "
  [key->messages]
  (swap! -registry merge key->messages)
  nil)


(defn undef
  "
  Undefine a message for the given sym/kw.
  "
  [kw-spec|sym-pred]
  (swap! -registry dissoc kw-spec|sym-pred)
  nil)


(defn undef-all
  "
  Undefine all the known messages.
  "
  []
  (reset! -registry nil)
  nil)


(defn explain-data
  "
  Like s/explain-data: takes a spec and a value
  and returns a map with the problem list. Each problem
  has clear message found by either a predicate or a spec.
  "
  [spec value]
  (when-let [explain
             (s/explain-data spec value)]
    (map-explain->errors explain)))


(defn explain
  "
  Prints the formatted output of explain-data.
  "
  [spec value]
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


(defn explain-str
  "
  Like explain, but returns the captured output as a string.
  "
  [spec value]
  (with-out-str
    (explain spec value)))


;;
;; EN defaults
;;

(soothe.core/def-many en/presets)



;; (def _form '(clojure.core/fn [%] (clojure.core/contains? % :bar)))

;; (s/conform ::missing-key _form)

;; (s/conform (s/cat :_1 '#{clojure.core/contains?}
;;                     :_2 '#{%}
;;                     :_3 keyword?) '(clojure.core/contains? % :bar))
