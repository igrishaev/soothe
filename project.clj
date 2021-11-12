(defproject com.github.igrishaev/soothe "0.1.0-SNAPSHOT"

  :plugins
  [[lein-cljsbuild "1.1.8"]]

  :dependencies
  [[org.clojure/clojurescript "1.10.891"]
   [javax.xml.bind/jaxb-api "2.3.1"]
   [org.glassfish.jaxb/jaxb-runtime "2.3.1"]]

  :description
  "Turn Clojure.spec errors into human-readable text."

  :url
  "https://github.com/igrishaev/soothe"

  :cljsbuild
  {:builds
   [{:source-paths ["src" "test"]
     :compiler {:output-to "target/tests.js"
                :output-dir "target"
                :main soothe.core-test
                :target :nodejs}}]}

  :license
  {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :profiles
  {:dev
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
