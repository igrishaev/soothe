
(alias '. 'soothe.core)

;; clojure.core

(./def `seq?                "The value must be a sequence.")
(./def `char?               "The value must be a char.")
(./def `string?             "The value must be a string.")
(./def `map?                "The value must be a map.")
(./def `vector?             "The value must be a vector.")
(./def `nil?                "The value must be nil.")
(./def `false?              "The value must be false.")
(./def `true?               "The value must be true.")
(./def `boolean?            "The value must be a boolean.")
(./def `some?               "The value must not be nil.")
(./def `symbol?             "The value must be a symbol.")
(./def `keyword?            "The value must be a keyword.")
(./def `delay?              "The value must be a delayed object.")
(./def `zero?               "The value must equal to zero.")
(./def `pos?                "The value must be a positive number.")
(./def `neg?                "The value must be a negative number.")
(./def `integer?            "The value must be an integer.")
(./def `even?               "The value must be an even number.")
(./def `odd?                "The value must be an odd number.")
(./def `int?                "The value must be a fixed precision integer.")
(./def `pos-int?            "The value must be a positive integer.")
(./def `neg-int?            "The value must be a negative integer.")
(./def `nat-int?            "The value must be a natural integer.")
(./def `double?             "The value must be a double.")
(./def `map-entry?          "The value must be a map entry.")
(./def `ident?              "The value must be either a symbol or a keyword. ")
(./def `simple-ident?       "The value must be a non-qualified symbol or a keyword.")
(./def `qualified-ident?    "The value must be a qualified symbol or a keyword.")
(./def `simple-symbol?      "The value must be a non-qualified symbol.")
(./def `qualified-symbol?   "The value must be a qualified symbol.")
(./def `simple-keyword?     "The value must be a non-qualified keyword.")
(./def `qualified-keyword?  "The value must be a qualified keyword.")
(./def `not-empty           "The value must be a non-empty collection.")
(./def `number?             "The value must be a number.")
(./def `ratio?              "The value must be a ratio.")
(./def `decimal?            "The value must be a decimal number.")
(./def `float?              "The value must be a float number.")
(./def `rational?           "The value must be a rational number.")
(./def `set?                "The value must be a set.")
(./def `special-symbol?     "The value must be a special symbol.")
(./def `var?                "The value must be a Var instance.")
(./def `bytes?              "The value must be a byte array.")
(./def `class?              "The value must be a Class.")
(./def `empty?              "The value must be an empty collection.")
(./def `coll?               "The value must be a collection.")
(./def `list?               "The value must be a list.")
(./def `seqable?            "The value must be a seqable object.")
(./def `ifn?                "The value must implement the IFn protocol.")
(./def `fn?                 "The value must be a function.")
(./def `associative?        "The value must support association.")
(./def `sequential?         "The value must implement the Sequential protocol.")
(./def `sorted?             "The value must support sorting.")
(./def `counted?            "The value must be counted.")
(./def `reversible?         "The value must be reversible.")
(./def `indexed?            "The value must support access by index.")
(./def `future?             "The value must be a Future object.")
(./def `inst?               "The value must be a date.")
(./def `uuid?               "The value must be a UUID.")

;; spec

(./def `s/spec?             "The value must be a spec.")
(./def `s/regex?            "The value must be a regex matcher.")

;; special cases

(./def ::missing-key
  (fn [{:keys [key]}]
    (format "The object misses the mandatory key '%s'."
            (-> key str (subs 1)))))

(./def ::default
  "The value is incorrect.")
