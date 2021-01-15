package it.uniroma2.netgroup.abe4jwt.showcase.client;

import org.eclipse.microprofile.config.Config;

import com.nimbusds.jose.util.Base64;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoProvider;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

@WebServlet(urlPatterns = "/authorize")
public class AuthorizationCodeServlet extends HttpServlet {

	private static final int MAX_ATTEMPT = 20;
	@Inject
	private Config config;
	static public Client theGoodClient;
	
	public static String _clientId;
	public static boolean IGNORE_HOSTNAME_VERIFIER=System.getenv("IGNORE_HOSTNAME_VERIFIER")!=null?
			System.getenv("IGNORE_HOSTNAME_VERIFIER").equalsIgnoreCase("true")
			:false;
	private static FakeHostnameVerifier fakeHostnameVerifier=new FakeHostnameVerifier();
	
	private String _redirectUri;
	private String _audienceUri;
	private String _scope;
	private String _ASUri;

	@Override
	public void init() throws ServletException {
		//Why this? see https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/client.html#d0e4974
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		
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
		
		System.out.println("Initializing... IGNORE_HOSTNAME_VERIFIER is:"+IGNORE_HOSTNAME_VERIFIER);
		for (int i=0;i<MAX_ATTEMPT;i++) {
			Client client = theGoodClient;
			if (client==null) client=IGNORE_HOSTNAME_VERIFIER?
										ClientBuilder.newBuilder().hostnameVerifier(fakeHostnameVerifier).build()
										:ClientBuilder.newClient();
			WebTarget resourceWebTarget;
			resourceWebTarget = client.target(getASUri())
					.path("/jwk")
					.queryParam("format", "pem");
			System.out.println("Sending request:"+resourceWebTarget.getUri()+
					"\n...attempt "+(i+1)+" of "+MAX_ATTEMPT);
			try {
				Response r=resourceWebTarget.request().get();
				if (r.getStatus()==200) {
					getServletContext().setAttribute("mpk",r.readEntity(String.class));
					AbeCryptoProvider _abeProvider = AbeCryptoFactory.get();
					_abeProvider.setMPKfromPEM((String)getServletContext().getAttribute("mpk"));
					System.out.println("Found a 'good' httpclient which has correctly DNS-resolved the target TLS IP address... "
							+ "retrieved mpk is:"+getServletContext().getAttribute("mpk"));
					theGoodClient=client;
					return;
				} else {
					System.out.println("Initialization error, cannot retrieve Master Public Key!"+
							"\nrequest returned code:"+r.getStatus()+"\nmessage:"+r.readEntity(String.class));
					theGoodClient=client;
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (resourceWebTarget!=null)
					System.out.println("Exception connecting to "+resourceWebTarget.getUri());
				System.out.println("Trying reconnect...");
				try {
					Thread.sleep(2000*(i+1));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private int getSecretKey() {
		WebTarget resourceWebTarget;
		Response r;
		//get the client's secret key
		String state=UUID.randomUUID().toString();
		getServletContext().setAttribute(AbstractServlet.CLIENT_LOCAL_STATE, state);
		final String targetUri=getASUri();
		if (theGoodClient==null) {
			System.out.println("FATAL: no 'good' httpclient found (please see "
					+ "AuthorizationCodeServlet.java source code for more info), cannot retrieve secret key!");
			return 400;
		}
		resourceWebTarget = theGoodClient.target(targetUri)
				.path("sk")
				.queryParam("url", _redirectUri)
				.queryParam("state", state);
		r=resourceWebTarget.request().get();
		int code=r.getStatus();
		System.out.println("Client's secret key has been requested to "+targetUri+"!"+
				"\nThe request returned code:"+code+" message:"+r.readEntity(String.class));
		return code;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//please check src/main/resources/META-INF/microprofile-config.properties for application properties
		if (_clientId==null) {
			_clientId=request.getRequestURL().toString();
			String extra=request.getPathInfo();
			if (extra==null) extra="";
			_clientId=_clientId.substring(0,_clientId.indexOf("/authorize"+extra));
			_redirectUri=_clientId+"/callback";
			System.out.println("This is the first request to the Client, performing " +
					"initialization by requesting secret key...\n"+
					"-original request URL:"+request.getRequestURL()+"\n"+
					"-clientId:"+_clientId+"\n"+
					"-redirectUri:"+_redirectUri+"\n"
					);			
			if (getSecretKey()!=200) {
				System.out.println("--- --- --- FATAL: Client's secret key request error, procedure halted! --- --- ---");
				//TODO: handle this more gently!!!
				System.exit(1);
				return;
			};
		}

		String state = UUID.randomUUID().toString();
		request.getSession().setAttribute(AbstractServlet.CLIENT_LOCAL_STATE, state);
		String authorizationLocation = getASUri() + "/authorize?response_type=code"
				+ "&client=" + _clientId
				+ "&redirect_uri=" + _redirectUri
				+ "&audience_uri=" + getAudienceUri()
				+ "&scope=" + getScope()
				+ "&state=" + state;
		System.out.println("\n\nLOGIN started, redirecting user to "+authorizationLocation);
		response.sendRedirect(authorizationLocation);
	}

	private String getScope() {
		if (_scope==null) _scope=config.getValue("client.scope", String.class);
		return _scope;
	}

	private String getAudienceUri() {
		if (_audienceUri==null) _audienceUri =  System.getenv("AUDIENCE_URI");
		if (_audienceUri!=null) System.out.println("Reverse proxy URI is PROXY_URI="+_audienceUri);
		else _audienceUri = config.getValue("PROXY_URI", String.class);
		return _audienceUri;
	}

	private String getASUri() {
		if (_ASUri==null) _ASUri = System.getenv("AS_URI");
		if (_ASUri!=null) System.out.println("Authorization Server URI is AS_URI="+_ASUri); 
		else _ASUri = config.getValue("AS_URI", String.class);
		return _ASUri;
	}
}
