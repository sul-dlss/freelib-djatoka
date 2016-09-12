FROM tomcat:9

COPY ./target/freelib-djatoka-1.1.3-SNAPSHOT.war /usr/local/tomcat/webapps/djatoka.war
COPY lib /usr/local/tomcat/lib
COPY lib/kdu_jni.jar /usr/local/tomcat/native-jni-lib
COPY lib/Linux-x86-64 /usr/local/tomcat/native-jni-lib
COPY lib/Linux-x86-64 /usr/bin

