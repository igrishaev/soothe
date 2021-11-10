(ns soothe.core-test
  (:require
   [soothe.core :as soo]

   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.test :refer [deftest is testing]]))


(s/def ::user
  (s/keys :req-un [:user/name
                   :user/age]))

(s/def :user/name string?)
(s/def :user/age int?)


(def user {:name "test"
           :age 42})


(deftest test-ok

  (is (nil? (soo/explain-data ::user user)))

  (is (=

       {:problems
        [{:message "The value must be a string." :path [:name] :val nil}
         {:message "The value must be a fixed precision integer." :path [:age] :val nil}]}

       (soo/explain-data
        ::user
        (assoc user
               :age nil
               :name nil))))

  (is (= {:problems
          [{:message "The object misses the mandatory key 'age'."
            :path []
            :val {:name "test"}}]}

         (soo/explain-data
          ::user
          (dissoc user :age)))))


(soo/def ::email
  (fn [_]
    "custom message for email spec"))


(soo/def `validate-email
  (fn [_]
    "custom message for email pred"))


(defn validate-email [string]
  (str/includes? string "@"))

(s/def ::email (s/and string? validate-email))
(s/def ::user2
  (s/keys :req-un [:user/name :user/age ::email]))


(deftest test-fn

  (is (nil? (soo/explain-data ::user2 (assoc user :email "test@test.com"))))

  (is (=

       {:problems
        [{:message "custom message for email pred"
          :path [:email]
          :val "sdfsf"}]}

       (soo/explain-data ::user2 (assoc user :email "sdfsf")))))
