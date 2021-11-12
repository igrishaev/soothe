
all: test-all
	lein install

repl:
	lein repl

.PHONY: test
test:
	lein test

test-js:
	lein with-profile +cljs cljsbuild once
	node target/tests.js

test-all: test test-js
