(ns org.kipz.rpm-version.core-t
  (:require [clojure.test :refer [deftest is]]
            [org.kipz.rpm-version.core :refer [compare-versions
                                               in-range?]]))

(deftest comparing-versions
  (is (not (compare-versions "1" "1")))
  (is (compare-versions "4.19.0a-1.el7_5" "4.19.0c-1.el7"))
  (is (compare-versions "4.19.0-1.el7_5" "4.21.0-1.el7"))
  (is (compare-versions "4.19.01-1.el7_5" "4.19.10-1.el7_5"))
  (is (not (compare-versions "4.19.0-1.el7_5" "4.19.0-1.el7")))


  (is (not (compare-versions "4.19.0-1.el7_5" "4.17.0-1.el7")))
  (is (not (compare-versions "4.19.01-1.el7_5" "4.19.1-1.el7_5")))
  (is (not (compare-versions "4.19.1-1.el7_5" "4.19.01-1.el7_5")))

  (is (not (compare-versions "4.19.1-1.el7_5" "4.19.1-01.el7_5")))
  (is (not (compare-versions "4.19.1-01.el7_5" "4.19.1-1.el7_5")))

  (is (not (compare-versions "4.19.1" "4.19.1")))

  (is (compare-versions "1.2.3-el7_5~snapshot1" "1.2.3-3-el7_5"))

  (is (not (compare-versions "1:0", "0:1")))
  (is (not (compare-versions "1:2", "1")))
  (is (compare-versions "0:4.19.1-1.el7_5", "2:4.19.1-1.el7_5"))
  (is (not (compare-versions "4:1.2.3-3-el7_5", "1.2.3-el7_5~snapshot1")))

  (is (not (compare-versions "2:4.19.01-1.el7_5", "4.19.1-1.el7_5")))
  (is (compare-versions "4.19.1-1.el7_5" "2:4.19.01-1.el7_5"))

  (is (compare-versions "4.19.01-1.el7_5", "2:4.19.1-1.el7_5"))
  (is (not (compare-versions "2:4.19.1-1.el7_5" "4.19.01-1.el7_5")))

  (is (compare-versions "4.19.0-1.el7_5", "12:4.19.0-1.el7"))
  (is (not (compare-versions "12:4.19.0-1.el7" "4.19.0-1.el7_5"))))

(deftest sorting
  (is (= ["1"
          "1.2.3-el7_5~snapshot1"
          "1.2.3-3-el7_5"
          "4.19.0a-1.el7_5"
          "4.19.0c-1.el7"
          "1:1"]
         (sort compare-versions ["1" "1:1" "4.19.0a-1.el7_5" "4.19.0c-1.el7" "1.2.3-el7_5~snapshot1" "1.2.3-3-el7_5"]))))
