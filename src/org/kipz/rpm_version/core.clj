(ns org.kipz.rpm-version.core
  (:require [clojure.string :as str]))

(def ^:private version-spec
  #"^(?:(\d+):)?(.*?)(?:-(.*))?$")

(defn parse-version
  "Return a tuple of [epoch version release] (see Readme) or nil"
  [version]
  (when (string? version)
    (when-let [[epoch version release] (not-empty (rest (re-matches version-spec version)))]
      {:epoch (or epoch "0")
       :version version
       :release release})))

(defn- normalize
  "Convert to numerical as needed"
  [v1 v2 c]
  (let [r (c v1 v2)]
    (cond
      (number? r) r
      (true? r) -1
      (c v2 v1) 1
      :else 0)))

(defn- compare-epoc
  [v1 v2]
  (< (Integer/parseInt (:epoch v1))
     (Integer/parseInt (:epoch v2))))

(defn- compare-in-order
  "Return first non-zero result"
  [v1 v2 & fns]
  (or (some->>
       fns
       (map (partial normalize v1 v2))
       (drop-while zero?)
       first)
      0))

(def ^:private version-split #"([a-zA-Z]+)|([0-9]+)|(~)")

(defn numeric?
  [s]
  (boolean
   (when-let [s (seq s)]
     (empty? (drop-while #(Character/isDigit %) s)))))

(defn- compare-tilde
  [v1 v2]
  (= "~" v1))

(defn- compare-alpha-vs-num
  [v1 v2]
  (boolean
   (and
    (not (numeric? v1))
    (numeric? v2))))

(defn- without-leading-zeros
  [some-str]
  (->> some-str
       (drop-while #(=  \0 %))
       (apply str)))

(defn- compare-without-zeros
  [v1 v2]
  (boolean
   (when (and v1 v2)
     (let [v1 (without-leading-zeros v1)
           v2 (without-leading-zeros v2)
           c1 (count v1)
           c2 (count v2)]
       (if (= c1 c2)
         (> 0 (compare v1 v2))
         (< c1 c2))))))

(defn- compare-seqs
  "Exhaust boths v1s and v2s"
  [v1s v2s fns]
  (loop [v1s v1s v2s v2s]
    (let [v1 (first v1s)
          v2 (first v2s)]
      (when (or v1 v2)
        (let [result (apply compare-in-order
                            v1 v2
                            fns)]
          (if (= 0 result)
            (recur (rest v1s) (rest v2s))
            result))))))

(def ^:private version-comparison-fns
  [compare-tilde
   compare-alpha-vs-num
   compare-without-zeros])

(defn- compare-version-parts
  [{v1 :version-parts} {v2 :version-parts}]
  (compare-seqs v1 v2 version-comparison-fns))

(defn- compare-release-parts
  [{v1 :release-parts} {v2 :release-parts}]
  (compare-seqs v1 v2 version-comparison-fns))

(defn- break-part
  [some-str]
  (when some-str
    (not-empty (flatten (map (comp #(remove nil? %) rest)
                             (re-seq version-split some-str))))))

(defn- break-parts
  [parsed-version]
  (when parsed-version
    (let [result (assoc parsed-version :version-parts (break-part (:version parsed-version)))]
      (if-let [release (:release parsed-version)]
        (assoc result :release-parts (break-part release))
        result))))

(defn compare-versions
  "Compare to rpm package version strings. Returns true if v1 is before/lower than v2"
  [v1s v2s]
  (boolean
   (if (and (string? v1s)
            (string? v2s)
            (= v1s v2s))
     false
     (let [v1 (break-parts (parse-version v1s))
           v2 (break-parts (parse-version v2s))]
       (when (and v1 v2)
         (> 0 (compare-in-order
               v1 v2
               compare-epoc
               compare-version-parts
               compare-release-parts)))))))

(def ^:private range-operators #"(\>=|\<=|\<|\>|=)")

(defn- split-ranges
  "Pre-clean ranges for easier version matching"
  [range-str]
  (str/split
   (str/replace
    (str/trim
     (str/replace
      range-str
      #"[\<\>=,&]"
      " "))
    #"\s+"
    " ")
   #" "))


(defn- compare-to-range
  [version operator range]
  (boolean
   (cond
     (= "=" operator)
     (= version range)

     (= "<" operator)
     (compare-versions version range)

     (= ">" operator)
     (compare-versions range version)

     (= "<=" operator)
     (or (= version range)
         (compare-versions version range))

     (= ">=" operator)
     (or (= version range)
         (compare-versions range version)))))

(defn in-range?
  "Is version in range string (e.g. < 12.23 & > 14.1~foo)"
  [version range]
  (boolean
   (when (and
          (string? version)
          (string? range))
     (let [[range-version1 range-version2] (split-ranges range)
           [operator1 operator2] (map second (re-seq range-operators range))]
       (when (and range-version1 range-version1)
         (and
          (compare-to-range version operator1 range-version1)
          (or
           (not (and range-version2 operator2))
           (compare-to-range version operator2 range-version2))))))))
