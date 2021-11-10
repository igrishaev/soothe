(ns soothe.core-test
  (:require
   [soothe.core :as soo]

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
  (is (nil? (soo/explain-data ::user user))))


(deftest test-wrong-type

  (is (=

       {:problems
        [{:message "The value must be a string." :path [:name] :val nil}]}

       (soo/explain-data
        ::user
        (assoc user
               :name nil)))))


(deftest test-missing-key

  (is (= {:problems
          [{:message "The object misses the mandatory key 'age'."
            :path []
            :val {:name "Test"}}]}

         (soo/explain-data
          ::user
          (dissoc user :age)))))


(deftest test-email-and-pred-not-string

  (is (= {:problems
          [{:message "The value must be a string."
            :path [:email]
            :val :not/string}]}

         (soo/explain-data
          ::user
          (assoc user :email :not/string)))))


(deftest test-email-and-pred-regex-fail

  (is (= {:problems
          [{:message "The value is incorrect."
            :path [:email]
            :val "not-an-email"}]}

         (soo/explain-data
          ::user
          (assoc user :email "not-an-email")))))


(deftest test-email-custom-message

  (soo/def :user/email "custom message for email")

  (is (= {:problems
          [{:message "custom message for email"
            :path [:email]
            :val "not-an-email"}]}

         (soo/explain-data
          ::user
          (assoc user :email "not-an-email"))))

  (soo/undef :user/email))


(deftest test-fn-message

  (soo/def ::user
    (fn [{:keys [val]}]
      (format "custom message from fn, val: %s" val)))

  (is (= {:problems
          [{:message "custom message from fn, 42"
            :path []
            :val 42}]}

         (soo/explain-data ::user 42)))

  (soo/undef ::user))


(deftest test-default-message

  (is (= {:problems
          [{:message "The value is incorrect."
            :path [:field-42]
            :val 0}]}

         (soo/explain-data ::user (assoc user :field-42 0)))))
