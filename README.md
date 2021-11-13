# Soothe

Clear error messages for Clojure.spec, extremely simple and robust.

[API Documentation](https://igrishaev.github.io/soothe/)

## Table of Contents

<!-- toc -->

- [Installation](#installation)
- [Concepts](#concepts)
- [Code Samples](#code-samples)
- [API](#api)
- [ClojureScript](#clojurescript)
- [Localization](#localization)
- [Known cases](#known-cases)
- [Contribute](#contribute)

<!-- tocstop -->

## Installation

- Leiningen/Boot

~~~clojure
[com.github.igrishaev/soothe "0.1.0"]
~~~

- clojure CLI/deps.edn

~~~clojure
com.github.igrishaev/soothe {:mvn/version "0.1.0"}
~~~

## Concepts

Clojure.spec is a piece of art yet misses some bits when dealing with error
messages.  The standard `s/explain-data` gives a raw machinery output that
bearly can be shown to the end user. This library is going to fix this.

The idea of Soothe is extreamly simple. The library keeps its private registry
of spec/pred => message pairs. The key is either a keyword referencing a spec or
a full-qualified symbol meaning a predicate. The value of this map is either a
plain string or a function that takes the problem map of the raw explain spec
data.

For example:

~~~clojure
{:some.ns/user
 "This is a wrong user."

 'other.ns/data-valid?
 "The data is invalid."

 :my.project.spec/item
 (fn [{:as problem :keys [pred in]}] ;; other spec problem keys
   (format "Build a custom message for this spec in runtime"))}
~~~

Soothe provides its own version of `explain-data`. When called, it prepares the
raw Spec explain data and then remaps it. For each problem, Soothe tries find a
message using this algorithm:

- when the `pred` field is a fully-qualified symbol, get the message from the
  registry. For example, `clojure.core/int?` resolves into something like `"The
  value must be an integer"`.

- TODO

## Code Samples

~~~clojure

;;
;; Imports
;;

(ns ...
  (:require
   [soothe.core :as sth]
   [clojure.spec.alpha :as s]))


;;
;; Define a spec
;;
(s/def :user/name string?)
(s/def :user/age int?)

(s/def :user/email
  (s/and
    string?
    (partial re-matches #"(.+?)@(.+?)")))

(s/def :user/field-42
  (fn [x]
    (= x 42)))

(s/def ::user
  (s/keys :req-un [:user/name
                   :user/age]
          :opt-un [:user/email
                   :user/field-42]))


;;
;; Data sample
;;

(def user
  {:name "Test" :age 42})


;;
;; no errors
;;
(sth/explain-data ::user user) ;; nil

;;
;; wrong type
;;
(sth/explain-data
  ::user
  (assoc user :name 42))

{:problems
  [{:message "The value must be a string."  ;; <<<
    :path [:name]
    :val 42}]}

;;
;; Missing key
;;
(sth/explain-data
  ::user (dissoc user :age))

{:problems
  [{:message "The object misses the mandatory key 'age'."  ;; <<<
    :path []
    :val {:name "Test"}}]}

;;
;; Custom predicate fails, no custom message defined
;;
(sth/explain-data
  ::user (assoc user :email "wrong-string"))

{:problems
 [{:message "The value is incorrect."  ;; <<<
   :path [:email]
   :val "wrong-string"}]}


;;
;; Define a cumstom message
;;
(sth/def :user/email "Wrong email.")

(sth/explain-data
  ::user (assoc user :email "wrong-string"))

{:problems
  [{:message "Wrong email."  ;; <<<
   :path [:email]
   :val "wrong-string"}]}

;;
;; A message for a custom predicate
;;
(defn some-complidated-check [value]
  (= value 100500))

(sth/def `some-complidated-check
  "The value did't match that complicated check.")

(s/def ::data
  (s/and int? some-complidated-check))

(sth/explain ::data -1)

{:problems
 [{:message "The value did't match that complicated check."  ;; <<<
   :path []
   :val -1}]}

;;
;; The message can be a function
;;
(sth/def :user/email
  (fn [{:as problem
        :keys [path pred val via in]}]
    (format "Custom error message for email, pred: %s" pred)))

(sth/explain-data
  ::user (assoc user :email "wrong-string"))

{:problems
 [{:message
   "Custom error message for email, pred: (clojure.core/partial clojure.core/re-matches #\"(.+?)@(.+?)\")"  ;; <<<
   :path [:email]
   :val "wrong-string"}]}

;;
;; Formatted output:
;;
(sth/explain
  ::user (dissoc user :age))

;; Problems:
;;
;; - The object misses the mandatory key 'age'.
;;   path: []
;;   value: {:name Test}

(sth/explain-str ::user (dissoc user :age))
;; returns the same output as a string

;;
;; Handling conformers
;;
(sth/def `->int
  "Cannot coerce the value to an integer.")

(s/def ::config
  (s/keys :req-un [:config/port
                   :config/timeout]))

(s/def :config/port
  (s/conformer ->int))

(s/def :config/timeout
  (s/conformer ->int))

(sth/explain-data
  ::config {:port "five" :timeout "dunno"})

{:problems
  [{:message "Cannot coerce the value to an integer."  ;; <<<
    :path [:port]
    :val "five"}
   {:message "Cannot coerce the value to an integer."  ;; <<<
    :path [:timeout]
    :val "dunno"}]}
~~~

[tests]: blob/master/test/soothe/core_test.cljc

For more examples, see [the unit tests][tests].





## API

## ClojureScript

## Localization

## Known cases

## Contribute

Ivan Grishaev, 2021
