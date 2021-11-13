(defproject com.github.igrishaev/soothe "0.1.1-SNAPSHOT"

  :description
  "Turn Clojure.spec errors into human-readable text."

  :url
  "https://github.com/igrishaev/soothe"

  :license
  {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org" :creds :gpg}}

  :codox
  {:output-path "gh-pages"}

  :release-tasks
  [["vcs" "assert-committed"]
   ["shell" "make" "test-all"]
   ["shell" "make" "gh-build"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["vcs" "commit"]
   ["vcs" "tag" "--no-sign"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "push"]]

  :profiles
  {:dev
   {:plugins [[lein-shell "0.5.0"]
              [lein-codox "0.10.7"]]
    :dependencies [[org.clojure/clojure "1.10.1"]]}

   :cljs
   {:cljsbuild
    {:builds
     [{:source-paths ["src" "test"]
       :compiler {:output-to "target/tests.js"
                  :output-dir "target"
                  :main soothe.core-test
                  :target :nodejs}}]}

    :plugins
    [[lein-cljsbuild "1.1.8"]]

    :dependencies
    [[org.clojure/clojurescript "1.10.891"]
     [javax.xml.bind/jaxb-api "2.3.1"]
     [org.glassfish.jaxb/jaxb-runtime "2.3.1"]]}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
