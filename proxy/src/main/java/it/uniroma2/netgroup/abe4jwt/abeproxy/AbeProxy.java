package it.uniroma2.netgroup.abe4jwt.abeproxy;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.SocketAddressResolver;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.nimbusds.jose.util.Base64URL;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoProvider;
import it.uniroma2.netgroup.abe4jwt.util.RandomString;
import it.uniroma2.netgroup.abe4jwt.util.StringReplacer;
import it.uniroma2.netgroup.abe4jwt.abeproxy.FakeHostnameVerifier;

public class AbeProxy extends ProxyServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8381980198784216830L;
	private static final int MAX_ATTEMPT = 20;
	//	private static String _mySelf; //no more used, local host address directly taken from the request
	private static String _authority;
	private static HashMap<String,String> sessions=new HashMap<String,String>(); 
	private String _proxyTo;
	private String _prefix;
	private String _MPKasPEM;
	private AbeCryptoProvider _abeProvider;
	private boolean __NO_SECRET_CACHE=false; //PAY ATTENTION: this will force a continuous client challenge authentication (set to 'true' ONLY when testing for performance evaluation)
	private String _protected;
	private String _audience;
	private RandomString _random;
	private int _attempt=0;
	

	public static boolean IGNORE_HOSTNAME_VERIFIER=System.getenv("IGNORE_HOSTNAME_VERIFIER")!=null?
			System.getenv("IGNORE_HOSTNAME_VERIFIER").equalsIgnoreCase("true")
			:false;
	HttpClient theGoodClient=null;
	
	public void init(ServletConfig config) throws ServletException {
		//please see WEB-INF/web.xml for config parameters definition
		super.init(config);	
		_authority = System.getenv("AS_URI");
		if (_authority==null) _authority = config.getInitParameter("authority");
		super._log.info(config.getServletName() + " Authorization Server URI is " + _authority);
		if (_authority==null)
			throw new UnavailableException("Init parameter 'authority' is required."); 
		_proxyTo = config.getInitParameter("proxyTo");
		if (_proxyTo == null)
			throw new UnavailableException("Init parameter 'proxyTo' is required.");
		String prefix = config.getInitParameter("prefix");
		if (prefix != null)
		{
			if (!prefix.startsWith("/"))
				throw new UnavailableException("Init parameter 'prefix' must start with a '/'.");
			_prefix = prefix;
		}
		String protect = config.getInitParameter("protected");
		if (protect != null)
		{
			if (!protect.startsWith(_prefix))
				throw new UnavailableException("Init parameter 'protected' must start with '"+_prefix+"'.");
			_protected = protect;
		}
		// Adjust prefix value to account for context path
		String contextPath = config.getServletContext().getContextPath();
		_prefix = _prefix == null ? contextPath : (contextPath + _prefix);

		_random=new RandomString();
		
		
		final String url = _authority + "/jwk?format=pem";
		SslContextFactory.Client sslContextFactoryClient = new SslContextFactory.Client();
		//TODO: this code requires an explicit truststore (key.p12) containing the AS certificate to be provided.
		//if you don't want to provide it explicitly, include the AS certificate into the default truststore within your java distribution, e.g.
		//keytool -exportcert -v -keystore /usr/src/mymaven/abe4jwt-pri/key.p12 -storepass initial -alias default -file fake-pwd.crt && \
		//keytool -importcert -v -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -alias fake-play-with-docker -file fake-pwd.crt -noprompt		
		sslContextFactoryClient.setTrustStorePath("key.p12");
		sslContextFactoryClient.setTrustStorePassword("initial");
		System.out.println("Initializing... IGNORE_HOSTNAME_VERIFIER is:"+IGNORE_HOSTNAME_VERIFIER);
		if (IGNORE_HOSTNAME_VERIFIER) {
			sslContextFactoryClient.setEndpointIdentificationAlgorithm(null);
			sslContextFactoryClient.setHostnameVerifier(new FakeHostnameVerifier());
		}
		HttpClient httpClient = new HttpClient(sslContextFactoryClient);
		
		//Why this?
		
		//Some DNS names pointing to container implementations (noticeably play-with-docker.com)
		//returns more than one IP address provided in the DNS A record. Only some of these IPs supports HTTPS,
		//while others don't and closes the connection. It is not possible to know it in advance, as they are
		//dynamically allocated by the container manager! As a workaround we try multiple attempts, trying to
		//be lucky (OK, I know, this is very quicky and odd!)
		
		//TODO: a better workaround would be to configure the underlying jaxrs client with a proper DNS resolver
		//which returns at each attempt a different IP address, but this appears to be complex at the time.
		//see https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/client.html#d0e4974
		//see also https://shibboleth.1660669.n2.nabble.com/Apache-HttpClient-and-multiple-A-records-td7628844.html
		//for jetty connectors see https://stackoverflow.com/questions/55609297/custom-hostname-resolver-for-jax-rs-client
		//and finally for apache client connectors see https://stackoverflow.com/questions/24350150/how-to-override-dns-in-http-connections-in-java
		
		for (int i=0;i<MAX_ATTEMPT;i++) {
			httpClient.setSocketAddressResolver(new SocketAddressResolver.Sync() {
				@Override
				public void resolve(String host, int port, Promise<List<InetSocketAddress>> promise)
				{
					try {
						InetAddress[] addresses = InetAddress.getAllByName(host);
						if (addresses.length==0) promise.failed(new UnknownHostException());
						else {
							List<InetSocketAddress> result = new ArrayList<>(1);
							result.add(new InetSocketAddress(addresses[_attempt%addresses.length], port));
							promise.succeeded(result);
						}
					}
					catch (Throwable x) {
						promise.failed(x);
					}
				}
			});
			super._log.info("[ABE-PROXY] Sending request MPK to: "+url+
					"\n...attempt "+(_attempt+1)+" of "+MAX_ATTEMPT);
			try {
				httpClient.start();
				ContentResponse r=httpClient.GET(url);
				if (r.getStatus()==200) {
					_MPKasPEM = r.getContentAsString();
					_abeProvider=AbeCryptoFactory.get();
					_abeProvider.setMPKfromPEM(_MPKasPEM);
					super._log.info("[ABE-PROXY] Received MPK from "+url+"\n"+_MPKasPEM);
					super._log.info(config.getServletName() + " @ " + _prefix + " to " + _proxyTo+". Protected path is "+_protected);
					return;
				} else {
					throw new UnavailableException("[ABE-PROXY] No MPK from "+url+"\n status: "+r.getStatus()+"\n"+r.getContentAsString());
				}
			} catch (Exception e) {
				e.printStackTrace();
				super._log.info("Exception connecting to "+url+
								"\nTrying reconnect...");
				_attempt++;
				try {
					httpClient.stop();
					Thread.sleep(2000*(i+1));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		super._log.info("[ABE-PROXY] FATAL: cannot retrieve MPK from "+url);
		throw new UnavailableException("[ABE-PROXY] FATAL: cannot retrieve MPK from "+url);
	}

	protected String rewriteTarget(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (!path.startsWith(_prefix))
			return null;

		StringBuilder uri = new StringBuilder(_proxyTo);
		if (_proxyTo.endsWith("/"))
			uri.setLength(uri.length() - 1);
		String rest = path.substring(_prefix.length());
		if (!rest.isEmpty())
		{
			if (!rest.startsWith("/"))
				uri.append("/");
			uri.append(rest);
		}

		String query = request.getQueryString();
		if (query != null)
		{
			// Is there at least one path segment ?
			String separator = "://";
			if (uri.indexOf("/", uri.indexOf(separator) + separator.length()) < 0)
				uri.append("/");
			uri.append("?").append(query);
		}
		URI rewrittenURI = URI.create(uri.toString()).normalize();

		if (!super.validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
			return null;

		return rewrittenURI.toString();
	}


	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//In a former version, clientId and userId were specified as request parameters, however this approach was a bit too restrictive.
		//		String clientId=request.getParameter("client"); //NO MORE USED: clientID is transmitted with the first request
		//		String userId=request.getParameter("user"); //NO MORE USED: userId not used anymore, as it is part of the resourceId	

		//Proxy discovers its own identity by examining the first incoming request
		if (_audience==null) {
			_audience=request.getRequestURL().toString();
			String extra=request.getPathInfo();
			if (extra!=null) _audience=_audience.substring(0,_audience.indexOf(extra)); //TODO: fix this!
			super._log.info("This is the first incoming request to the Proxy, performing " +
					"initialization by discovering self identifier...\n"+
					"-original request URL:"+request.getRequestURL()+"\n"+
					"-audience (Proxy's identifer):"+_audience);	
		}
		
		String resourceId=request.getPathInfo();
		super._log.info("[ABE-PROXY] ResourceId:"+resourceId);
		//check if on the protected path, if not, just proxy to the target
		if (!resourceId.startsWith(_protected)) {
			super.service(request,response);
			return;
		}
		//the resource is on the protected path, check credential
		String[] clientCredentials = extractCredentials(request.getHeader(HttpHeader.AUTHORIZATION.asString()));
		if (clientCredentials.length==2) {
			String credClientId=clientCredentials[0];
			String credSecret=clientCredentials[1];
			//			if (super._log.isDebugEnabled()) 
			super._log.info("[ABE-PROXY] Received credentials: "+credClientId+":"+credSecret);
			if (!credClientId.isEmpty()) {
				String storedSecret=sessions.get(getKey(resourceId, credClientId));
				if (credSecret!=null&&credSecret.equals(storedSecret)) {
					super._log.info("[ABE-PROXY] credentials provided are valid "+credClientId+":"+credSecret);
					super.service(request, response);
					if(__NO_SECRET_CACHE) sessions.remove(getKey(resourceId, credClientId));
					return;
				} else {
					super._log.info("[ABE-PROXY] Invalid credentials provided "+credClientId+":"+credSecret+", stored secret was "+credClientId+":"+storedSecret);
					String realm = generateRealm(resourceId,credClientId,_audience);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setHeader("WWW-Authenticate", "Basic realm=\""+realm+"@"+_authority+"\"");
					super._log.warn("[ABE-PROXY] Please authenticate with challenge: "+realm);
				}
			}
		}
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No client ID specified.");
	}

	private String[] extractCredentials(String authHeader) {
		//		if (super._log.isDebugEnabled()) 
		super._log.info("[ABE-PROXY] Received auth header: "+authHeader);
		if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
			String decoded=new String(Base64.getDecoder().decode(authHeader.substring(6).trim()));
			return new String[] {decoded.replaceAll("(.*)(:)([^:]*)", "$1"), 
					decoded.replaceAll("(.*)(:)([^:]*)", "$3") 
			}; //parsing method to avoid mismatch with any ':' char inside the clientId!!!
		}

		return new String[]{};
	}	

	private String getKey(String resourceId, String clientId) {
		return "client:"+clientId+"&resource:"+resourceId+".secret";
	}

	private String generateRealm(String resourceId, String clientId, String audience) {
//		String secret=UUID.randomUUID().toString().replaceAll("-", "");
		String secret=_random.nextString();
		sessions.put(getKey(resourceId, clientId), secret);
		String realm=null;
		if (_abeProvider!=null) {
			try {
				//encrypt secret using: clientId, userId, resourceId, thisServerId, local time (LocalDateTime)
				StringBuffer encKey=new StringBuffer(); //will hold simmetric encryption key
				byte[] plainText=secret.getBytes();
				final String encryptInput="issuer:"+_authority+
						//						"|user:"+userId+ //userId not used anymore, as it is part of the resourceId
						"|client_id:"+clientId+
						"|audience:"+audience+
						"|scope:"+StringReplacer.replace(resourceId)+//workaround to encode valid path and fragment into ABE policy
						"|exp:"+(new SimpleDateFormat("yyyy-MM-dd")).format(Date.from(Instant.now()));  //for the time being, we don't handle time, just assume expiration is today at midnight
				super._log.info("[ABE-PROXY] generating secret:"+secret
						+ "\n encrypt attributes: "+encryptInput);
				long t=System.currentTimeMillis();
				Base64URL encrypted=_abeProvider.encrypt(encryptInput, plainText, encKey);
				//then, encrypt once more using: clientID
				StringBuffer encKey2=new StringBuffer(); //will hold simmetric encryption key
				Base64URL encrypted2=_abeProvider.encrypt("client_id:"+clientId, (encKey.toString()+"_"+encrypted.toString()).getBytes(), encKey2);
				realm=encKey2.toString()+"_"+encrypted2.toString();
				//				if (super._log.isDebugEnabled()) 
				System.out.println("[PERFORMANCE-encryption-] "+(System.currentTimeMillis()-t)+","+secret.length()+","+realm.length());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { //SEVERE! NO ABEPROVIDER found! Falling back to dummy authentication (realm=secret)
			//			if (super._log.isDebugEnabled()) 
			super._log.info("[ABE-PROXY] WARNING NO ABE PROVIDER DETECTED FALLING BACK TO DUMMY MODE");
			realm=secret;
		}
		return realm;
	}
}
