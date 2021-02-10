package it.uniroma2.netgroup.abe4jwt.showcase.client;

import org.eclipse.microprofile.config.Config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.jose.KPABEDecrypter;
import net.minidev.json.JSONObject;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Base64;

@WebServlet(urlPatterns = "/callback")
public class CallbackServlet extends AbstractServlet {

	@Inject
	private Config config;

	//Used when receiving the code parameter from the AS (similarly to OpenID Connect 1.0 Server Flow)
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//        String clientId = config.getValue("client.clientId", String.class);
		//        String clientSecret = config.getValue("client.clientSecret", String.class);

		//Error:
		String error = request.getParameter("error");
		if (error != null) {
			request.setAttribute("error", error);
			dispatch("/logoff", request, response);
			return;
		}
		String localState = (String) request.getSession().getAttribute(AbstractServlet.CLIENT_LOCAL_STATE);
		if (localState!=null&&!localState.equals(request.getParameter("state"))) {
			request.setAttribute("error", "The state attribute doesn't match !!");
			dispatch("/logoff", request, response);
			return;
		}

		String code = request.getParameter("code")
				.replaceAll(" ", "+"); //added to avoid unwanted characters substitution (previously all "+" were converted into blanks)
//		String clientId=config.getValue("client.clientId", String.class);
		String clientKey=(String) getServletContext().getAttribute(AbstractServlet.CLIENT_KEY); //clientKey is set in AuthorizationCodeServlet.init()
		try {
			System.out.println("Decrypting JWT using clientKey:"+clientKey);
			EncryptedJWT jwt = EncryptedJWT.parse(code);
//			System.out.println("Decrypting JWT:"+jwt.getParsedString());
			jwt.decrypt(new KPABEDecrypter(new Base64URL(clientKey)));
			try {
				Payload p=jwt.getPayload();
				request.getSession().setAttribute(JWT,p);
				JSONObject o=p.toJSONObject();
				request.getSession().setAttribute(EPHKEY, o.getAsString(EPHKEY));
				final String userId=o.getAsString(SUB);
				final String scope=o.getAsString(SCOPE);
				if (userId!=null) request.getSession().setAttribute(SUB, userId);
				else request.getSession().removeAttribute(SUB); 
				request.getSession().setAttribute(SCOPE, o.getAsString(SCOPE));
				if (userId!=null) System.out.println("LOGIN completed for user "+userId+" allowed scope: "+scope);
//				System.out.println("JWT decrypted payload is:"+p.toString());				
			} catch (NullPointerException e) {
				System.out.println("--- --- --- ALERT DECRYPTION FAILED! --- --- ---");
				request.getSession().setAttribute(JWT,"JWT cannot be decrypted");
			}
			dispatch("/index.jsp", request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	//Used to receive the client's secret key (see JWKEndpoint in AS). 
	//The client's key request procedure starts up when AuthorizationCodeServlet.init() method is invoked.
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String localState = (String) getServletContext().getAttribute(AbstractServlet.CLIENT_LOCAL_STATE);
		if (localState!=null&&!localState.equals(request.getParameter("state"))) {
			System.out.println("Client's secret response received but the state parameter is not matching:"
					+ "\nlocal state:"+localState
					+ "\nreceived:"+request.getParameter("state")
					);
			return; //wrong state!
		}
		ByteArrayOutputStream writer=new ByteArrayOutputStream();
		transferTo(request.getInputStream(),writer);
		String key=writer.toString();
		getServletContext().setAttribute(AbstractServlet.CLIENT_KEY,key);
		System.out.println("Client's secret key is:"+key);
	}	
	
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
    private long transferTo(InputStream in, OutputStream out) throws IOException {
        if (out==null) throw new IOException("Output stream cannot be null!");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
} 
