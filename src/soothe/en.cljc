(ns soothe.en
  (:require

   #?(:cljs
      [soothe.js :refer [format]])

   #?(:clj  [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])))


;; (alias 'sth 'soothe.core)

(def presets
  {`seq?                "The value must be a sequence."
   `char?               "The value must be a char."
   `string?             "The value must be a string."
   `map?                "The value must be a map."
   `vector?             "The value must be a vector."
   `nil?                "The value must be nil."
   `false?              "The value must be false."
   `true?               "The value must be true."
   `boolean?            "The value must be a boolean."
   `some?               "The value must not be nil."
   `symbol?             "The value must be a symbol."
   `keyword?            "The value must be a keyword."
   `delay?              "The value must be a delayed object."
   `zero?               "The value must equal to zero."
   `pos?                "The value must be a positive number."
   `neg?                "The value must be a negative number."
   `integer?            "The value must be an integer."
   `even?               "The value must be an even number."
   `odd?                "The value must be an odd number."
   `int?                "The value must be an integer."
   `pos-int?            "The value must be a positive integer."
   `neg-int?            "The value must be a negative integer."
   `nat-int?            "The value must be a natural integer."
   `double?             "The value must be a double."
   `map-entry?          "The value must be a map entry."
   `ident?              "The value must be either a symbol or a keyword. "
   `simple-ident?       "The value must be a non-qualified symbol or a keyword."
   `qualified-ident?    "The value must be a qualified symbol or a keyword."
   `simple-symbol?      "The value must be a non-qualified symbol."
   `qualified-symbol?   "The value must be a qualified symbol."
   `simple-keyword?     "The value must be a non-qualified keyword."
   `qualified-keyword?  "The value must be a qualified keyword."
   `not-empty           "The value must be a non-empty collection."
   `number?             "The value must be a number."
   `ratio?              "The value must be a ratio."
   `decimal?            "The value must be a decimal number."
   `float?              "The value must be a float number."
   `rational?           "The value must be a rational number."
   `set?                "The value must be a set."
   `special-symbol?     "The value must be a special symbol."
   `var?                "The value must be a Var instance."
   `bytes?              "The value must be a byte array."
   `class?              "The value must be a Class."
   `empty?              "The value must be an empty collection."
   `coll?               "The value must be a collection."
   `list?               "The value must be a list."
   `seqable?            "The value must be a seqable object."
   `ifn?                "The value must implement the IFn protocol."
   `fn?                 "The value must be a function."
   `associative?        "The value must support association."
   `sequential?         "The value must implement the Sequential protocol."
   `sorted?             "The value must support sorting."
   `counted?            "The value must be counted."
   `reversible?         "The value must be reversible."
   `indexed?            "The value must support access by index."
   `future?             "The value must be a Future object."
   `inst?               "The value must be a date."
   `uuid?               "The value must be a UUID."

   ;; spec

   `s/spec?             "The value must be a spec."
   `s/regex?            "The value must be a regex matcher."

   ;; special cases

   :soothe.core/missing-key
   (fn [{:keys [key]}]
     (format "The object misses the mandatory key '%s'."
             (-> key str (subs 1))))

   :soothe.core/default
   "The value is incorrect."})
