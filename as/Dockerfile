#ABE4JWT DEMO PACKAGE
#
#This file builds the Authorization Server (AS). As a prerequisite, you need to have built the ABE_OPEN_LIBERTY_TEMPLATE first.
#To this end, please look for the corresponding Dockerfile into parent folder (main project folder).
#
#- to build: 
# docker build -t as . --no-cache
#
#- to run:
# export IGNORE_HOSTNAME_VERIFIER=<if "true", the Authorization Server will skip TLS hostname verify procedure>
# docker run -dp 9443:9443 -e IGNORE_HOSTNAME_VERIFIER as
#

#4. Authorization Server
FROM abe-open-liberty-template AS AS
USER root
RUN  set -xe ; \
	keytool -exportcert -v -keystore /usr/src/mymaven/abe4jwt/key.p12 -storepass initial -alias default -file fake-pwd.crt && \
	keytool -importcert -v -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -alias fake-play-with-docker -file fake-pwd.crt -noprompt
USER 1001
ENV KEYSTORE_REQUIRED "false"
ENV IGNORE_HOSTNAME_VERIFIER "true"
ENV VERBOSE "true" 
RUN set -xe ; \
	cp /usr/src/mymaven/abe4jwt/as/target/liberty/wlp/usr/servers/defaultServer/server.xml /config/ && \
	cp /usr/src/mymaven/abe4jwt/as/target/as.war /config/dropins/ && \
	/opt/ol/helpers/build/configure.sh