(ns severin.pool.monger-test
  (:require [clojure.test :refer :all]
            [severin.core :as severin])
  (:use [severin.pool.monger]))

(deftest monger-test
  (let [p (severin/make-pool)]
    (testing "create & dispose resource"
      (severin/with-pool p [r "monger://localhost/test"]
        (is (= "localhost" (-> (:conn r) .getAddress .getHost)))
        (is (= 27017 (-> (:conn r) .getAddress .getPort)))
        (is (= "test" (-> (:db r) .getName))))
      (testing "recycle resource"
        (severin/with-pool p [r "monger://localhost/foobar"]
          (is (= "localhost" (-> (:conn r) .getAddress .getHost)))
          (is (= 27017 (-> (:conn r) .getAddress .getPort)))
          (is (= "foobar" (-> (:db r) .getName))))))))
