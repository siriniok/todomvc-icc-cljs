(ns todomvc-icc-cljs.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [todomvc-icc-cljs.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
