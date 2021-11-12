(ns soothe.core-test
  (:require
   [soothe.core :as sth]

   [clojure.string :as str]

   #?(:cljs
      [soothe.js :refer [format]])

   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])

   #?(:clj [clojure.test :as t :refer [deftest is testing]]
      :cljs [cljs.test :as t :refer [deftest is testing]])))


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


(deftest test-missing-quialified-key

  (s/def ::sample
    (s/keys :req [:sample/foo]))

  (is (= {:problems
           [{:message "The object misses the mandatory key 'sample/foo'."
             :path []
             :val {:foo 1}}]}

         (sth/explain-data ::sample {:foo 1}))))


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

- The value must be an integer.
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

    #?(:clj
       (try
         (Integer/parseInt val)
         (catch Exception e
           ::s/invalid))

       :cljs
       (let [result (js/parseInt val)]
         (if (js/isNaN result)
           ::s/invalid
           result)))
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
        [{:message "The value must be an integer."
          :path []
          :val "a"}]}

       (sth/explain-data int? "a"))))



(deftest test-coll-of-pred-is-unqualified

  (is (=

       {:problems
        [{:message "The value is incorrect."
          :path [3]
          :val "a"}]}

       (sth/explain-data
        (s/coll-of int?) [1 2 3 "a"]))))


(s/def ::is42?
  (fn [x]
    (= x 42)))


(sth/def ::is42?
  "The value is not 42.")


(deftest test-coll-of-pred-is-spec

  (is (=

       {:problems
        [{:message "The value is not 42."
          :path [2]
          :val -1}]}

       (sth/explain-data
        (s/coll-of ::is42?) [42 42 -1]))))


(deftest test-map-of

  (is (=

       {:problems
        [{:message "The value must be a number."
          :path ["aaa" 1]
          :val "test"}
         {:message "The value must be a string."
          :path [42333 0]
          :val 42333}]}

       (sth/explain-data (s/map-of string? number?)
                         {"aaa" "test" 42333 3}))))


(s/def ::master-spec integer?)

(s/def ::minor-spec1 ::master-spec)
(s/def ::minor-spec2 ::master-spec)

(s/def ::proxy-case
  (s/keys :req-un [::minor-spec1
                   ::minor-spec2]))


(deftest test-proxy-spec

  (is (=

       {:problems
        [{:message "The value must be an integer."
          :path [:minor-spec2]
          :val "2"}]}

       (sth/explain-data ::proxy-case {:minor-spec1 1
                                       :minor-spec2 "2"}))))


(s/def ::lvl1-spec
  (s/keys :req-un [::lvl2-spec]))

(s/def ::lvl2-spec
  (fn [x]
    (= x 42)))


(deftest test-spec-ierarchy

  (sth/def ::lvl1-spec "message for level 1")
  (sth/def ::lvl2-spec "message for level 2")

  (is (=

       {:problems
        [{:message "message for level 2"
          :path [:lvl2-spec]
          :val "haha"}]}

       (sth/explain-data ::lvl1-spec
                         {:lvl2-spec "haha"})))

  (sth/undef ::lvl2-spec)

  (is (=

       {:problems
        [{:message "message for level 1"
          :path [:lvl2-spec]
          :val "haha"}]}

       (sth/explain-data ::lvl1-spec
                         {:lvl2-spec "haha"})))

  (sth/undef ::lvl1-spec))


(deftest test-missing-key-priority

  (sth/def ::lvl1-spec "message for level 1")

  (is (=

       {:problems
        [{:message "The object misses the mandatory key 'lvl2-spec'."
          :path []
          :val {:no-required-key "passed"}}]}

       (sth/explain-data ::lvl1-spec
                         {:no-required-key "passed"})))

  (sth/undef ::lvl1-spec))



#?(:cljs

   (do

     (defn -main [& _]
       (t/run-tests))

     (set! *main-cli-fn* -main)))
