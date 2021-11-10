(ns quiet.core-test
  (:require
   [quiet.core :as q]

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

  (is (nil? (q/explain-data ::user user)))

  (is (= {:problems
          [{:message "The value must be a string."
            :path [:name]
            :val nil}
           {:message "The value must be an integer."
            :path [:age]
            :val nil}]}

         (q/explain-data
          ::user
          (assoc user
                 :age nil
                 :name nil))))

  (is (= {:problems
          [{:message "The map misses the key."
            :path []
            :val {:name "test"}}]}

         (q/explain-data
          ::user
          (dissoc user :age)))))


(q/def ::email
  (fn [_]
    "custom message for email spec"))


(q/def `validate-email
  (fn [_]
    "custom message for email pred"))


(defn validate-email [string]
  (str/includes? string "@"))

(s/def ::email (s/and string? validate-email))
(s/def ::user2
  (s/keys :req-un [:user/name :user/age ::email]))


(deftest test-fn



  (is (nil? (q/explain-data ::user2 (assoc user :email "test@test.com"))))

  (is (= 1
         (q/explain-data ::user2 (assoc user :email "sdfsf"))))


  )
