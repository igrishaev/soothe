
repl:
	lein repl

.PHONY: test
test:
	lein test

test-js:
	lein cljsbuild once
	node target/tests.js
