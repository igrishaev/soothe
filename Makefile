
PROJECT := soothe

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


gh-init:
	git clone -b gh-pages --single-branch git@github.com:igrishaev/{PROJECT}.git gh-pages


gh-build:
	lein codox
	cd gh-pages && git add -A && git commit -m "docs updated" && git push


toc-install:
	npm install --save markdown-toc

toc-build:
	node_modules/.bin/markdown-toc -i README.md

.PHONY: release
release:
	lein release
