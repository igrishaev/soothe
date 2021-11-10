(defproject com.github.igrishaev/soothe "0.1.0-SNAPSHOT"

  :description
  "Turn Clojure.spec errors into human-readable text."

  :url
  "https://github.com/igrishaev/soothe"

  :license
  {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :profiles
  {:dev
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
