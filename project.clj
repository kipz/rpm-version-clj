(defproject org.kipz/rpm-version-clj "0.0.2"
  :description "Parse and compare rpm package versions in clojure"
  :url "https://github.com/kipz/rpm-version-clj"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.3"]]}}
  :deploy-repositories {"releases" {:url "https://repo.clojars.org" :creds :gpg}})
