#! /bin/bash


MAVEN_OPTS="-Djava.library.path=target/lib" mvn -U -Dmaven.test.skip=true jetty:run-exploded
