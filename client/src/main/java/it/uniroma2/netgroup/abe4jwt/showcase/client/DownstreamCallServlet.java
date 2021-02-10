package it.uniroma2.netgroup.abe4jwt.showcase.client;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoProvider;
import it.uniroma2.netgroup.abe4jwt.jose.KPABEDecrypter;
import it.uniroma2.netgroup.abe4jwt.util.StringReplacer;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@WebServlet(urlPatterns = "/downstream")
public class DownstreamCallServlet extends HttpServlet {

	@Inject
	private Config config;
	private AbeCryptoProvider abeProvider;
	private String _audienceUri;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse outResponse) throws ServletException, IOException {
		System.out.println(request.getMethod()+" "+request.getPathInfo());
		final String userId=(String) request.getSession().getAttribute(AbstractServlet.SUB);
//		final String clientId=config.getValue("client.clientId", String.class);
		final String clientId=AuthorizationCodeServlet._clientId;
		if (clientId==null) {
			System.out.println("--- --- --- ALERT: ClientId not set, call url \\authorize first (i.e., just login for the first time) to inizialize the Client --- --- ---");
			outResponse.sendError(400);
			return;
		}
		final String resourceId=request.getPathInfo();
		final String key=getKey(userId,resourceId);
		if (request.getSession().getAttribute(key)==null) request.getSession().setAttribute(key, ""); //workaround to prevent the synchronized block below to throw null pointer exception :)

		synchronized (request.getSession().getAttribute(key)) {
			String secret=(String) request.getSession().getAttribute(key);
			String data=null;
			if ("POST".equals(request.getMethod())) {
				StringBuffer buf=new StringBuffer();
				String line;
				while ((line=request.getReader().readLine())!=null) 
					buf.append(line+"\n");
				data=buf.toString();
			}
			long startProcedure=System.currentTimeMillis();
			String contentType=request.getContentType();

			Response response = elicitate(userId, clientId, resourceId, secret,data,contentType);
			if (response.getStatusInfo()!=Response.Status.OK) {
//				System.out.println("[PERFORMANCE-intermediate] "+(System.currentTimeMillis()-startProcedure));			
				//try and update the challenge 
				String authenticate=response.getHeaderString("WWW-Authenticate");//
				if (authenticate!=null) {
					System.out.println("DECRYPTING WWW-Authenticate header for "+key);
					String[] realm=authenticate.replaceAll("(Basic\\s+realm\\s*=\\s*\\\")([^\\\"]+)(\\\")", "$2").split("@");
					secret=realm[0]; //TODO: calculate secret!!!				
					String clientKey=(String) getServletContext().getAttribute(AbstractServlet.CLIENT_KEY); //clientKey is set in AuthorizationCodeServlet.init()
					String ephkey=(String) request.getSession().getAttribute(AbstractServlet.EPHKEY);
					if (ephkey!=null) try {
						String[] parts=realm[0].split("_");
						abeProvider=AbeCryptoFactory.get();
						long startDecryption=System.currentTimeMillis();
						String plainText=new String(abeProvider.decrypt(new Base64URL(clientKey), new Base64URL(parts[0]), new Base64URL(parts[1])));
						System.out.println("DECRYPTION PASS 1 OK. Proof of Possession achieved");
						String[] parts2=plainText.split("_");
						secret=new String(abeProvider.decrypt(
								new Base64URL(ephkey), 
								new Base64URL(parts2[0]), 
								new Base64URL(parts2[1])));
						long endDecryption=System.currentTimeMillis();
						System.out.println("DECRYPTION PASS 2 OK. Secret is:"+secret);
						request.getSession().setAttribute(key, secret);
						response = elicitate(userId, clientId, resourceId, secret, data, contentType);
					} catch (Exception e) {
						System.out.println("DECRYPTION ERROR:"+e+"\n"+e.getMessage()+"\n\n--> Did you provide the correct authorization? Are you calling allowed URIs?"
								+ "\n Check ephKey generation on the Client and AS <--"
								+ "\n\n");
						e.printStackTrace();
					} else {
						System.out.println("\n\n--> No ephkey found in session, did you provide login & consent?\n\n");
					}
				}
			}

			outResponse.setContentType(response.getHeaderString("Content-Type"));
			try {
				outResponse.setStatus(response.getStatus());
				String body=response.readEntity(String.class);
				outResponse.getWriter().println(body);
			} catch (Exception e) {
				e.printStackTrace();
			}
			outResponse.getWriter().close();
			long endProcedure=System.currentTimeMillis();
			System.out.println("Procedure completed in "+(endProcedure-startProcedure)+" ms");
			return;        
		}
	}

	private Response elicitate(String userId, String clientId, String resourceId, String secret, String post, String contentType) {
		Client client = AuthorizationCodeServlet.theGoodClient;
		WebTarget resourceWebTarget = client.target(getAudienceUri())
				.path(resourceId);
		//				.queryParam("client", clientId) //NO MORE USED: clientId is transmitted with the first request (with null password)
		//				.queryParam("user", userId); //NO MORE USED: userId should be part of the resource
		System.out.println("Connecting to uri="+resourceWebTarget.getUri()+
				"\n  Data to post="+post+
				"\n  Authorization header (user:password)="+clientId+":"+secret+
				"\n  Content-Type="+contentType);

		Invocation.Builder invocationBuilder = resourceWebTarget.request()
				.header("Authorization", "Basic "+Base64.encode(clientId+":"+secret))
				.header("Content-Type", contentType);
		Response r;
		if (post!=null) {
			r=invocationBuilder.post(Entity.entity(post, contentType /*MediaType.APPLICATION_JSON*/));
		} else { 
			r=invocationBuilder.get();
		}
		System.out.println("Received response:"+r.getStatus());
//		System.out.println("Received response:"+r.getStatus()+"\nWWW-Authenticate header:"+r.getHeaderString("WWW-Authenticate")+"\n");
		//+r.readEntity(String.class)); Unfortunately we cannot do this! It will consume the response entity that will be no more available to the caller!!
		return r;
	}

	private String getKey(String userId, String resourceId) {
		return "user="+userId+"&resource="+resourceId+".secret";
	}
	
	private String getAudienceUri() {
		if (_audienceUri==null) _audienceUri =  System.getenv("PROXY_URI");
		if (_audienceUri==null) _audienceUri = config.getValue("PROXY_URI", String.class);
		return _audienceUri;
	}

}