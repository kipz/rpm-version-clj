# rpm-version-clj

[![Clojars Project](https://img.shields.io/clojars/v/org.kipz/rpm-version-clj.svg)](https://clojars.org/org.kipz/rpm-version-clj)

Parse rpm package version scheme as per:


Thanks to https://github.com/anchore/grype from which I've pulled some test data.

```clj
[org.kipz/rpm-version-clj "<some version>"]
```

## Usage from Clojure

### Parse a version

```clj
(:require [org.kipz.rpm-version.core :refer [parse-version]])
;; returns nil if can't parse
(parse-version "1.2.3-el7_5~snapshot1")

;=>
{:epoch "0", :version "1.2.3", :release "el7_5~snapshot1"}
```

### Compare two versions

```clj
(:require [org.kipz.rpm-version.core :refer [compare-versions]])
(compare-versions "1.2.3-el7_5~snapshot1" "1.2.3-3-el7_5")
; => true first arg is lower/before second
```

### Sorting

As per normal Clojure awesomeness, we can use it as a normal comparator

```clj
(sort compare-versions ["1" "1:1" "4.19.0a-1.el7_5" "4.19.0c-1.el7" "1.2.3-el7_5~snapshot1" "1.2.3-3-el7_5"])
; => ["1" "1.2.3-el7_5~snapshot1" "1.2.3-3-el7_5" "4.19.0a-1.el7_5" "4.19.0c-1.el7" "1:1"]
```

### Range checking

Easily check if a version is in a particular range (two ranges are supported optionally separated by an &)

The following operators are allowed: `< > <= >= =`

```clj
(:require [org.kipz.rpm-version.core :refer [in-range?]])
(in-range? "1.2.3-el7_5~snapshot1" "< 1")
; => false
(in-range? "1.2.3-el7_5~snapshot1" "<= 1.2.3-3-el7_5")
; => true
```
