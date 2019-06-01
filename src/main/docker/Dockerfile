# Pull base image
FROM tomcat:9-jdk11

# Maintainer
MAINTAINER "darius.sas@outlook.com"

ENV JAVA_OPTS=-Denvironment=production

COPY /astracker-0.6.war /usr/local/tomcat/webapps/astracker.web
COPY tomcat-users.xml /usr/local/tomcat/conf/
COPY context.xml /usr/local/tomcat/conf/

EXPOSE 8080