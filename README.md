
<img align="left" src="art/real-soothe.jpeg">

# Soothe

Clear error messages for Clojure.spec, extremely simple and robust.

[API Documentation](https://igrishaev.github.io/soothe/)

---

## Table of Contents

<!-- toc -->

- [Installation](#installation)
- [Concepts](#concepts)
- [TL;DR: Code Samples](#tldr-code-samples)
- [The API](#the-api)
  * [Special messages](#special-messages)
- [Pre-defined messages & Localization](#pre-defined-messages--localization)
- [ClojureScript](#clojurescript)
- [Best practices & Known cases](#best-practices--known-cases)

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
bearly can be shown to the end-user. This library is going to fix this.

The idea of Soothe is extremely simple. The library keeps its private registry
of spec/pred => message pairs. The key is either a keyword referencing a spec or
a fully-qualified symbol meaning a predicate. The value of this map is either a
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
raw Spec explain data and then remaps it. For each problem, Soothe tries to find
a message using the following algorithm.

- When the `pred` field is a fully-qualified symbol, get the message from the
  registry. For example, `clojure.core/int?` resolves into something like `"The
  value must be an integer"`.

- When the `pred` is something different, try the `via` vector of specs. The
  algorithm iterates the vector in reverse order. The first spec which has a
  message in the registry will succeed.

- A special case when an `s/keys` spec misses a required key.

- Another special case when the spec is wrapped with `s/conformer`.

- The default message gets resolved.

## TL;DR: Code Samples

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

## The API

Define a message for a spec or a predicate using the `soothe.core/def` function:

~~~clojure
(defn my-predicate [x]
  ...)

(sth/def `my-predicate "Some message")
~~~

Use fully-qualified symbols, not simple ones. In the example above, the backtick
expands the symbol to the full form (with the current namespace).

Defining a message for a spec:

~~~clojure
(s/def ::user (s/keys ...))
(sth/def ::user "Message for the user spec")
~~~

The message might be a function that takes a preblem map and returns a string:

~~~clojure
(sth/def ::user
  (fn [problem]
    (format "A custom message ... %s" ...)))
~~~

The library handles the case when the predicate is wrapped into the
`s/conformer` spec. Soothe tries to find a message for the nested predicate if
possible:

~~~clojure
(defn ->int
  [val]
  (cond
    (int? val)
    val
    (string? val)
    ;; (... try to parse the string ...)
    :else
    ::s/invalid))

(s/def ::port ->int)

(sth/def `->int "Cannot coerce the value to an integer.")

(sth/explain-data ::port "dunno")
;; you'll get "Cannot coerce the value to an integer."
~~~

### Special messages

There are two *special messages* at the moment. The first one is the
`:soothe.core/missing-key` keyword which is used when a map misses a key. The
default implementation is:

~~~clojure
:soothe.core/missing-key
(fn [{:keys [key]}]
  (format "The object misses the mandatory key '%s'."
          (-> key str (subs 1))))
~~~

The library adds the `key` field into the problem map when detecting this case.

The second special message is `:soothe.core/default`. The default implementation
is just a string `"The value is incorrect."` You're welcome to register a
function for that key with a custom function.

Use `(sth/def-many {...})` function to define several key/message pairs at once
passing them as a map. The `(sth/undef ...)` function removes a message for the
passed key. To wipe all the messages, use `(sth/undef-all)`.

## Pre-defined messages & Localization

[en]: blob/master/src/soothe/en.cljc

The library ships predefined messages for all the `clojure.core` predicates:
`int?`, `string?`, `uuid?` and so forth. They locate in the [en.cljs module][en]
wich gets loaded automatically once you import `soothe.core`.

There is also a Russian version of the messages provided with the `soothe.ru`
module. Once loaded, it overrides the messages in the registry. Just import it
somewhere in your project:

~~~clojure
(ns ...
  (:require
    [soothe.core :as sth]
    soothe.ru ;; RU messages for spec
    ...))
~~~

You're welcome to submit your localized messages with a pull request.

## ClojureScript

Soothe is fully compatible with ClojureScript and thus can be used on the
frontend.

## Best practices & Known cases

- Declare the messages right after you're declared the specs or predicates, for
  example:

~~~clojure
(s/def ::my-spec ...) ;; your spec
(sth/def ::my-spec "...") ;; the message

;; or

(defn check-email [string]...)
(sth/def `check-email "...")
~~~

But don't put them in another namespace.

- Some specs spoil the predicates, for example, `s/coll-of`. Imagine you have a
  spec like this one:

~~~clojure
(s/def ::my-items (s/coll-of int?))
~~~

Now, if one of the items fails, the predicate will be not `'clojure.core/int?`
but just a `'int?` which leads to the default error message. To handle this,
bind the predicate to a spec and pass the spec:

~~~clojure
(s/def ::int? int?)
(s/def ::my-items (s/coll-of ::int?))
(sth/def ::int? "The value must be an integer.")
~~~

With this approach, the library will return the right error message.

---

Ivan Grishaev, 2021
