
(alias '. 'soothe.core)

(./def `seq?           "The value must be a sequence.")
(./def `char?          "The value must be a char.")
(./def `string?        "The value must be a string.")
(./def `map?           "The value must be a map.")
(./def `vector?        "The value must be a vector.")
(./def `nil?           "The value must be nil.")
(./def `false?         "The value must be false.")
(./def `true?          "The value must be true.")
(./def `boolean?       "The value must be a boolean.")
(./def `some?          "The value must not be nil.")
(./def `symbol?        "The value must be a symbol.")
(./def `keyword?       "The value must be a keyword.")
(./def `delay?         "The value must be a delayed object.")
(./def `zero?          "The value must equal to zero.")
(./def `pos?           "The value must be a positive number.")
(./def `neg?           "The value must be a negative number.")
(./def `integer?       "The value must be an integer.")
(./def `even?          "The value must be an even number.")
(./def `odd?           "The value must be an odd number.")
(./def `pos-int?       "The value must be a positive integer.")
(./def `neg-int?       "The value must be a negative integer.")



;; nat-int?
;; double?
;; map-entry?
;; ident?
;; simple-ident?
;; qualified-ident?
;; simple-symbol?
;; qualified-symbol?
;; simple-keyword?
;; qualified-keyword?
;; not-empty
;; number?
;; ratio?
;; decimal?
;; float?
;; rational?
;; set?
;; special-symbol?
;; var?
;; bytes?
;; class?
;; empty?
;; coll?
;; list?
;; seqable?
;; ifn?
;; fn?
;; associative?
;; sequential?
;; sorted?
;; counted?
;; reversible?
;; indexed?
;; future?
;; inst?
;; uuid?


;; ;; spec
;; spec?
;; regex?

;; ;; string
;; blank?




(./def ::missing-key "The map misses the key.")
(./def ::default "The message is not found.")
