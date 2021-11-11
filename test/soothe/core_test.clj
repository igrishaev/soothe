(ns soothe.core-test
  (:require
   [soothe.core :as sth]

   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.test :refer [deftest is testing]]))


(s/def ::user
  (s/keys :req-un [:user/name
                   :user/age]
          :opt-un [:user/email
                   :user/field-42]))

(s/def :user/name
  string?)

(s/def :user/age
  int?)

(s/def :user/email
  (s/and string? (partial re-matches #"(.+?)@(.+?)")))


(s/def :user/field-42
  (fn [x]
    (= x 42)))


(def user
  {:name "Test"
   :age 42})


(deftest test-no-errors
  (is (nil? (sth/explain-data ::user user))))


(deftest test-wrong-type

  (is (=

       {:problems
        [{:message "The value must be a string." :path [:name] :val nil}]}

       (sth/explain-data
        ::user
        (assoc user
               :name nil)))))


(deftest test-missing-key

  (is (= {:problems
          [{:message "The object misses the mandatory key 'age'."
            :path []
            :val {:name "Test"}}]}

         (sth/explain-data
          ::user
          (dissoc user :age)))))


(deftest test-email-and-pred-not-string

  (is (= {:problems
          [{:message "The value must be a string."
            :path [:email]
            :val :not/string}]}

         (sth/explain-data
          ::user
          (assoc user :email :not/string)))))


(deftest test-email-and-pred-regex-fail

  (is (= {:problems
          [{:message "The value is incorrect."
            :path [:email]
            :val "not-an-email"}]}

         (sth/explain-data
          ::user
          (assoc user :email "not-an-email")))))


(deftest test-email-custom-message

  (sth/def :user/email "custom message for email")

  (is (= {:problems
          [{:message "custom message for email"
            :path [:email]
            :val "not-an-email"}]}

         (sth/explain-data
          ::user
          (assoc user :email "not-an-email"))))

  (sth/undef :user/email))


(deftest test-fn-message

  (sth/def :user/field-42
    (fn [{:keys [val]}]
      (format "custom message for the field-42, val: %s" val)))

  (is (=

       {:problems
        [{:message "custom message for the field-42, val: 0"
          :path [:field-42]
          :val 0}]}

         (sth/explain-data ::user (assoc user :field-42 0))))

  (sth/undef :user/field-42))


(deftest test-default-message

  (is (= {:problems
          [{:message "The value is incorrect."
            :path [:field-42]
            :val 0}]}

         (sth/explain-data ::user (assoc user :field-42 0)))))


(def expected-report
  (str/trim "
Problems:

- The value must be a string.
  path: [:name]
  value: nil

- The value must be a fixed precision integer.
  path: [:age]
  value: -0
"))


(deftest test-explain-str

  (is (=

       expected-report

       (str/trim
        (sth/explain-str
         ::user
         (assoc user :name nil :age "-0"))))))


(deftest test-explain-print

  (is (=

       expected-report

       (str/trim
        (with-out-str
          (sth/explain
           ::user
           (assoc user :name nil :age "-0")))))))


(defn ->int
  [val]
  (cond
    (int? val)
    val
    (string? val)
    (try
      (Integer/parseInt val)
      (catch Exception e
        ::s/invalid))
    :else
    ::s/invalid))


(sth/def `->int
  "Cannot coerce the value to an integer.")


(s/def ::config
  (s/keys :req-un [:config/port
                   :config/timeout]))

(s/def :config/port
  (s/conformer ->int))

(s/def :config/timeout
  (s/conformer ->int))


(deftest test-conformer-ok

  (is (=

       {:problems
        [{:message "Cannot coerce the value to an integer."
          :path [:port]
          :val "five"}
         {:message "Cannot coerce the value to an integer."
          :path [:timeout]
          :val "dunno"}]}

       (sth/explain-data
        ::config {:port "five" :timeout "dunno"}))))


(deftest test-plain-data

  (is (=

       {:problems
        [{:message "The value must be a fixed precision integer."
          :path []
          :val "a"}]}

       (sth/explain-data int? "a"))))


(deftest test-coll-of

  (is (=

       {:problems
        [{:message "The value must be an integer."
          :path [3]
          :val "a"}]}

       (sth/explain-data (s/coll-of integer?) [1 2 3 "a"]))))
