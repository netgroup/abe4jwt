package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.net.URI;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.Config;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.util.Base64URL;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;


@ApplicationScoped
@Path("sk")
public class ClientSecretKeyEndpoint {


	@Inject
	private Config config;
	static public Client theGoodClient=null;
	public static boolean IGNORE_HOSTNAME_VERIFIER=System.getenv("IGNORE_HOSTNAME_VERIFIER")!=null?
			System.getenv("IGNORE_HOSTNAME_VERIFIER").equalsIgnoreCase("true")
			:false;
	private static FakeHostnameVerifier fakeHostnameVerifier=new FakeHostnameVerifier();
	private static final int MAX_ATTEMPT = 20;

	@GET
	public Response getClientKey(@QueryParam("redirect_uri") String url, @QueryParam("state") String state) {
		try {

			URI location = UriBuilder.fromUri(url)
					.queryParam("state", state!=null?state:"")
					.build();
			boolean https="https".equals(location.getScheme().toLowerCase());
			if (https||"http".equals(location.getScheme().toLowerCase())) {
				StringBuilder clientId=new StringBuilder(location.getScheme()+"://"+location.getHost());
				if (location.getPort()>-1) clientId.append(":"+location.getPort());
				String path=location.getPath();
				clientId.append(path.substring(0,path.lastIndexOf("/")));
				System.out.println("Generating key for client: "+clientId+"\nurl:"+url); 
				//key must be in this form: client_id:<client-uri>
				String clientKey=AbeCryptoFactory.get().keyGen("client_id:"+clientId.toString()).toString();
				//encrypt the client's key using AES 128 GCM
				KeyGenerator keyGen = KeyGenerator.getInstance("AES");
				keyGen.init(EncryptionMethod.A128GCM.cekBitLength());
		        JWEObject jweObject = new JWEObject(new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM), 
		        									new Payload(clientKey));
		        SecretKey AESKey=keyGen.generateKey();
		        jweObject.encrypt(new DirectEncrypter(AESKey));
		        //ship the encrypted client's key
				String returnString=ship(jweObject.serialize(),location.toString());
				//return the AES 128 GCM key used in encrypting the client's key
				String aesKey=Base64URL.encode(AESKey.getEncoded()).toString();
				System.out.println("Client's key: "+clientKey+
						"\nencrypted with AES key: "+aesKey+
						"\nand shipped to client. Response: "+returnString);
				return Response.ok(aesKey).build();
			}
			return Response.serverError().entity("ERROR:"+url+" is not a HTTP(S) URL").build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity("ABEProvider error: "+e.getMessage()).build();
		}
	}


	
	private String ship(String key, String url) {
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
		
		if (theGoodClient==null)
			System.out.println("Initializing... IGNORE_HOSTNAME_VERIFIER is:"+IGNORE_HOSTNAME_VERIFIER);
		for (int i=0;i<MAX_ATTEMPT;i++) {
			Client client = theGoodClient;
			if (client==null) client=IGNORE_HOSTNAME_VERIFIER?
					ClientBuilder.newBuilder().hostnameVerifier(fakeHostnameVerifier).build()
					:ClientBuilder.newClient();
					Response r=null;
					System.out.println("Shipping key:"+key+" to "+url+
							"\n...connection attempt "+(i+1)+" of "+MAX_ATTEMPT);
					try {
						r = client.target(url).request().post(Entity.entity(key, MediaType.TEXT_PLAIN));
						System.out.println("Found a 'good' httpclient which has correctly DNS-resolved the target TLS supporting IP address... ");
						theGoodClient=client;
						return r.getStatus()+"\n"+r.readEntity(String.class);
					} catch (Exception e) {
						e.printStackTrace();
						if (r!=null)
							System.out.println("Exception connecting to "+url);
						System.out.println("Trying reconnect...");
						try {
							Thread.sleep(2000*(i+1));
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}

		}
		return "400\nClient error cannot find a 'good' httpclient... try again";
	}
}
