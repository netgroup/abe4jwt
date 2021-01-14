#ABE4JWT DEMO PACKAGE
#
#This file builds the Client. As a prerequisite, you need to have built the ABE_OPEN_LIBERTY_TEMPLATE first.
#To this end, please look for the corresponding Dockerfile into parent folder (main project folder).
#
#- to build: 
# docker build -t client . --no-cache
#
#- to run:
# export IGNORE_HOSTNAME_VERIFIER=<if "true", the Client will skip TLS hostname verify procedure>
# export AS_URI=<URL where your Application Server runs>
# export PROXY_URI=<URL where your Proxy runs>
# docker run -dp 9543:9543 -e AS_URI -e PROXY_URI -e IGNORE_HOSTNAME_VERIFIER client

#6. Client
FROM abe-open-liberty-template AS CLIENT
USER root
RUN  set -xe ; \
	keytool -exportcert -v -keystore /usr/src/mymaven/abe4jwt/key.p12 -storepass initial -alias default -file fake-pwd.crt && \
	keytool -importcert -v -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -alias fake-play-with-docker -file fake-pwd.crt -noprompt
USER 1001
ENV KEYSTORE_REQUIRED "false"
ENV IGNORE_HOSTNAME_VERIFIER "true"
ENV VERBOSE "true"
RUN  set -xe ; \
	cp /usr/src/mymaven/abe4jwt/client/target/liberty/wlp/usr/servers/defaultServer/server.xml /config/ && \
	cp /usr/src/mymaven/abe4jwt/client/target/client.war /config/dropins/ && \
	/opt/ol/helpers/build/configure.sh