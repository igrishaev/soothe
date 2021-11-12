(ns soothe.ru
  (:require
   [soothe.core :as sth]

   #?(:clj  [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])

   #?(:cljs
      [soothe.js :refer [format]])))


(def presets
  {`seq?                "Значение должно быть последовательностью."
   `char?               "Значение должно быть буквой."
   `string?             "Значение должно быть строкой"
   `map?                "Значение должно быть словарём."
   `vector?             "Значение должно быть вектором."
   `nil?                "Значение должно быть nil."
   `false?              "Значение должно быть ложью."
   `true?               "Значение должно быть истиной."
   `boolean?            "Значение должно быть логического типа."
   `some?               "Значение не должно быть nil."
   `symbol?             "Значение должно быть символом"
   `keyword?            "Значение должно быть кейвордом."
   `delay?              "Значение должно быть Delayed-объектом."
   `zero?               "Значение должно быть равно нулю."
   `pos?                "Значение должно быть положительным числом."
   `neg?                "Значение должно быть отрицательным числом."
   `integer?            "Значение должно быть целым числом."
   `even?               "Значение должно быть чётным числом."
   `odd?                "Значение должно быть нечётным числом."
   `int?                "Значение должно быть целым числом."
   `pos-int?            "Значение должно быть положительным целым числом."
   `neg-int?            "Значение должно быть отрицательным целым числом."
   `nat-int?            "Значение должно быть натуральным целым числом."
   `double?             "Значение должно быть числом двойной точности."
   `map-entry?          "Значение должно быть элементом словаря."
   `ident?              "Значение должно быть символом или кейвордом."
   `simple-ident?       "Значение должно быть простым символом или кейвордом."
   `qualified-ident?    "Значение должно быть квалифицированным символом."

   `simple-symbol?      "Значение должно быть  a non-qualified symbol."
   `qualified-symbol?   "Значение должно быть  a qualified symbol."
   `simple-keyword?     "Значение должно быть  a non-qualified keyword."
   `qualified-keyword?  "Значение должно быть  a qualified keyword."
   `not-empty           "Значение должно быть  a non-empty collection."
   `number?             "Значение должно быть  a number."
   `ratio?              "Значение должно быть  a ratio."
   `decimal?            "Значение должно быть  a decimal number."
   `float?              "Значение должно быть  a float number."
   `rational?           "Значение должно быть  a rational number."
   `set?                "Значение должно быть  a set."
   `special-symbol?     "Значение должно быть  a special symbol."
   `var?                "Значение должно быть  a Var instance."
   `bytes?              "Значение должно быть  a byte array."
   `class?              "Значение должно быть  a Class."
   `empty?              "Значение должно быть  an empty collection."
   `coll?               "Значение должно быть  a collection."
   `list?               "Значение должно быть  a list."
   `seqable?            "Значение должно быть  a seqable object."
   `ifn?                "The value must implement the IFn protocol."
   `fn?                 "Значение должно быть  a function."
   `associative?        "The value must support association."
   `sequential?         "The value must implement the Sequential protocol."
   `sorted?             "The value must support sorting."
   `counted?            "Значение должно быть  counted."
   `reversible?         "Значение должно быть  reversible."
   `indexed?            "The value must support access by index."
   `future?             "Значение должно быть  a Future object."
   `inst?               "Значение должно быть  a date."
   `uuid?               "Значение должно быть  a UUID."

   ;; spec

   `s/spec?             "Значение должно быть  a spec."
   `s/regex?            "Значение должно быть  a regex matcher."

   ;; special cases

   ::sth/missing-key
   (fn [{:keys [key]}]
     (format "Отсутствует обязательный ключ'%s'."
             (-> key str (subs 1))))

   ::sth/default
   "Неверное значение."})


(sth/defmulti presets)
