#ABE4JWT DEMO PACKAGE
#
#This file builds the Reverse Proxy (+Resource Server). As a prerequisite, you need to have built the ABE_OPEN_LIBERTY_TEMPLATE first.
#To this end, please look for the corresponding Dockerfile into parent folder (main project folder).
#
#- to build: 
# docker build -t proxy . --no-cache
#
#- to run:
# export IGNORE_HOSTNAME_VERIFIER=<if "true", the Reverse Proxy will skip TLS hostname verify procedure>
# export AS_URI=<URL where your Application Server runs>
# docker run -dp 8443:8443 -e AS_URI -e IGNORE_HOSTNAME_VERIFIER proxy
#

#5. a.Resource Server
FROM abe-open-liberty-template AS PROXY
USER root
RUN  set -xe ; \
	keytool -exportcert -v -keystore /usr/src/mymaven/abe4jwt/key.p12 -storepass initial -alias default -file fake-pwd.crt && \
	keytool -importcert -v -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -alias fake-play-with-docker -file fake-pwd.crt -noprompt
USER 1001
ENV KEYSTORE_REQUIRED "false"
ENV VERBOSE "true"
RUN  set -xe ; \
	 cp /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/servers/defaultServer/server.xml /config/ && \
	 cp /usr/src/mymaven/abe4jwt/rs/target/rs.war /config/dropins/ && \
	 cp /usr/src/mymaven/abe4jwt/rs/target/liberty/wlp/usr/shared/resources/*.jar /opt/ol/wlp/usr/shared/resources/ && \
	 /opt/ol/helpers/build/configure.sh
#b.Jetty reverse Proxy
COPY --from=jetty:9.4.34-jre11-slim --chown=1001:0 /usr/local/jetty /usr/local/jetty 
COPY --from=jetty:9.4.34-jre11-slim --chown=1001:0 /var/lib/jetty /var/lib/jetty
ENV PATH=/usr/local/jetty/bin:$PATH
ENV JETTY_HOME="/usr/local/jetty"
ENV JETTY_BASE="/var/lib/jetty"
ENV IGNORE_HOSTNAME_VERIFIER "true"
RUN set -xe ; \
	mkdir -p /var/lib/jetty/etc && \
	mkdir -p /var/lib/jetty/webapps/WEB-INF && \
	cp /usr/src/mymaven/abe4jwt/key.p12 /usr/local/jetty/ && \
	#TODO: a workaround for the hardcoded path to keystore in AbeProxy.init() method. Fix this to refer to legacy folder (/jetty/etc)
	cp /usr/src/mymaven/abe4jwt/key.p12 /var/lib/jetty/ && \
	cp /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy.war  /var/lib/jetty/webapps/ && \
	cp /usr/src/mymaven/abe4jwt/proxy/target/new-abe-proxy/WEB-INF/web.xml  /var/lib/jetty/webapps/WEB-INF/ && \
	cp /usr/src/mymaven/abe4jwt/proxy/target/classes/*.xml /var/lib/jetty/etc/ && \
	cd $JETTY_BASE && \
	java -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start=ssl,https
CMD sh -c "cd $JETTY_BASE && java -jar /usr/local/jetty/start.jar & /opt/ol/wlp/bin/server run defaultServer"
