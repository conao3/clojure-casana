.PHONY: run
run:
	clojure -M:run -- $(ARGS)

.PHONY: build
build:
	clojure -T:build uber

.PHONY: test
test:
	clojure -X:test cognitect.test-runner.api/test

.PHONY: clean
clean:
	clojure -T:build clean
