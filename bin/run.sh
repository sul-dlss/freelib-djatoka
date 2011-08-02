#! /bin/bash

MAVEN_OPTS="-Djava.library.path=../target/lib" mvn jetty:run-exploded
