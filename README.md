# Soothe

Clear error messages for Clojure.spec.

## Table of Contents

<!-- toc -->

- [Installation](#installation)
- [TL;DR/Examples](#tldrexamples)
- [Usage](#usage)
- [ClojureScript](#clojurescript)
- [Algorithm](#algorithm)
- [Localization](#localization)
- [Known cases](#known-cases)
- [Contribute](#contribute)

<!-- tocstop -->

## Installation

TODO: deploy to Clojars

## TL;DR/Examples

~~~clojure

;;
;; Imports
;;

(ns ...
  (:require
   [soothe.core :as sth]
   [clojure.spec.alpha :as s]))


;;
;; Define a User spec
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
;; Examples
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
  [{:message "The value must be a string."
    :path [:name]
    :val 42}]}

;;
;; Missing key
;;
(sth/explain-data
  ::user (dissoc user :age))

{:problems
  [{:message "The object misses the mandatory key 'age'."
    :path []
    :val {:name "Test"}}]}

;;
;; Custom predicate fails, no custom message defined
;;

(sth/explain-data
  ::user (assoc user :email "wrong-string"))

{:problems
 [{:message "The value is incorrect."
   :path [:email]
   :val "wrong-string"}]}


;;
;; Define a cumstom message
;;

(sth/def :user/email "Wrong email.")

(sth/explain-data ::user (assoc user :email "wrong-string"))

{:problems [{:message "Wrong email." :path [:email] :val "wrong-string"}]}

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
 [{:message "The value did't match that complicated check."
   :path []
   :val -1}]}

;;
;; The message can be a function
;;

(sth/def :user/email
  (fn [{:as problem
        :keys [path pred val via in]}]
    (format "Custom error message for email, pred: %s" pred)))

(sth/explain-data ::user (assoc user :email "wrong-string"))

{:problems
 [{:message
   "Custom error message for email, pred: (clojure.core/partial clojure.core/re-matches #\"(.+?)@(.+?)\")"
   :path [:email]
   :val "wrong-string"}]}
~~~

[tests]: blob/master/test/soothe/core_test.cljc

For more examples, see [the unit tests][tests].

## Usage

## ClojureScript

## Algorithm

## Localization

## Known cases

## Contribute

Ivan Grishaev, 2021
