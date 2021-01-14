#ABE4JWT DEMO PACKAGE
#
#This file builds the Open Liberty Server Template used later to host the Authorization Server, Reverse Proxy (+Resource Server) and Client components.
#
#- to build: 
# docker build -t abe-open-liberty-template .  --no-cache
#
# NOTE: after building, you'll have to build and run each component in the following order:
#
# Authorization Server
# Reverse Proxy (+Resource Server)
# Client 
#
# Please take a look to the corresponding Dockerfile into each subfolder for more info.
#

#1. OpenABE Builder
FROM debian:10.7 AS OPENABE_BUILD
#Quirky code, TODO: remove sudo afterwards, enable ssl verification giving the right certificates, consider a slimmer distro...
RUN /bin/bash -c "apt-get -y update && \
	apt-get -y --no-install-recommends install sudo git && \
	git -c http.sslVerify=false clone https://github.com/zeutro/openabe && cd openabe && \
	. ./env \
	&& ./deps/install_pkgs.sh \
	&& export LD_LIBRARY_PATH=: \
	&& make \
	&& make install && \
	. ./env"

#2. Maven Builder
FROM maven:3-openjdk-11 AS MAVEN_BUILD
WORKDIR /usr/src/mymaven
RUN set -xe ; \
	git clone https://github.com/netgroup/abe4jwt && \
	cd abe4jwt/jwt && mvn clean install && \ 
	cd ../as && mvn clean package && \
	cd ../rs && mvn clean package && \
	cd ../client && mvn clean package && \
	cd ../proxy && mvn clean package

#3. Openliberty template 
FROM open-liberty:20.0.0.9-full-java8-openj9 AS ABE_OPEN_LIBERTY_TEMPLATE
#why not FROM open-liberty:20.0.0.12-full-java8-openj9? see issue https://github.com/OpenLiberty/open-liberty/issues/15305
#copy and install openabe files
USER root
RUN set -xe ; \
	apt-get update && \
	apt-get install make && \
	mkdir -p /openabe/deps/root && \
	mkdir -p /openabe/root/lib && \
	mkdir -p /openabe/root/include && \
	chown -R 1001:0 /openabe
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/Makefile /openabe
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/env /openabe
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/deps/root/lib /openabe/deps/root/lib
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/deps/root/include /openabe/deps/root/include
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/deps/root/bin /openabe/deps/root/bin
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/root/lib /openabe/root/lib
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/root/include /openabe/root/include
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/src /openabe/src
COPY --from=OPENABE_BUILD --chown=1001:0  /openabe/cli /openabe/cli
RUN /bin/bash -c "cd /openabe && . ./env && make install"
ENV LD_LIBRARY_PATH /openabe/deps/root/lib:/openabe/root/lib:$LD_LIBRARY_PATH
USER 1001
#copy mvn artifacts
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/key.p12 /usr/src/mymaven/abe4jwt/key.p12
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/as/target/liberty/wlp/usr/servers/defaultServer/server.xml /usr/src/mymaven/abe4jwt/as/target/liberty/wlp/usr/servers/defaultServer/server.xml 
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/as/target/as.war /usr/src/mymaven/abe4jwt/as/target/as.war
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/servers/defaultServer/server.xml /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/servers/defaultServer/server.xml
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/rs/target/rs.war /usr/src/mymaven/abe4jwt/rs/target/rs.war
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/shared/resources/*.jar /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/shared/resources/
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy.war /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy.war
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy/WEB-INF/web.xml /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy/WEB-INF/web.xml
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/proxy/target/classes/*.xml /usr/src/mymaven/abe4jwt/proxy/target/classes/
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/client/target/liberty/wlp/usr/servers/defaultServer/server.xml /usr/src/mymaven/abe4jwt/client/target/liberty/wlp/usr/servers/defaultServer/server.xml
COPY --from=MAVEN_BUILD --chown=1001:0 /usr/src/mymaven/abe4jwt/client/target/client.war /usr/src/mymaven/abe4jwt/client/target/client.war
#tell openliberty to not create a new keystore
RUN set -xe ; \
	echo "<server></server>" > /config/configDropins/defaults/keystore.xml