(ns severin.pool.monger-test
  (:require [clojure.test :refer :all]
            [severin.core :as severin])
  (:use [severin.pool.monger]))

(deftest monger-test
  (testing "monger pool"
    (let [p (severin/make-pool)
          r (severin/create! p "monger://localhost/test")]
      (is (= "localhost" (-> (:conn r) .getAddress .getHost)))
      (is (= 27017 (-> (:conn r) .getAddress .getPort)))
      (is (= "test" (-> (:db r) .getName)))
      (severin/dispose! p r)
      (let [r (severin/create! p "monger://localhost/foobar")]
        (is (= "foobar" (-> (:db r) .getName)))))))
