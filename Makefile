.PHONY: check
check:
	$(MAKE) fmt
	$(MAKE) lint

.PHONY: fmt
fmt:
	cljstyle fix

.PHONY: lint
lint:
	clj-kondo --lint src test

.PHONY: run
run:
	clojure -M:run -- $(ARGS)

.PHONY: build
build:
	clojure -T:build uber

.PHONY: test
test:
	clojure -M:test

.PHONY: clean
clean:
	clojure -T:build clean
