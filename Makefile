SHELL := /bin/bash
.SILENT:
.PHONY: all install test clean package show-deps

all:
	mvn -q dependency:build-classpath compile -DincludeScope=runtime -Dmdep.outputFile=target/.classpath -Dmaven.compiler.debug=false
	echo "To continuously build the javascript, run (yarn install && yarn watch)"

install:
	mvn -q install

test:
	mvn -q test

clean:
	mvn -q clean

package:
	mvn -q -DincludeScope=runtime dependency:copy-dependencies package

show-deps:
	mvn dependency:tree
