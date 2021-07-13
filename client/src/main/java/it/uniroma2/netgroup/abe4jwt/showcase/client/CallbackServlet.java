package it.uniroma2.netgroup.abe4jwt.showcase.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.config.Config;

import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;

import it.uniroma2.netgroup.abe4jwt.jose.KPABEDecrypter;
import net.minidev.json.JSONObject;

@WebServlet(urlPatterns = "/callback")
public class CallbackServlet extends AbstractServlet {

	private static final int MAX_ATTEMPT = 4;
	
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
	
	//Used to asynchronously receive the client's secret key (see ClientSecretKeyEndpoint.java in AS). 
	//As the client's key is encrypted using AES GCM, this procedure stores the wrapped key in the servlet's context
	//The client's key request procedure starts up when AuthorizationCodeServlet.init() method is invoked.
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String localState = (String) getServletContext().getAttribute(AbstractServlet.CLIENT_LOCAL_STATE);
		if (localState!=null&&!localState.equals(request.getParameter("state"))) {
			System.out.println("The state parameter is not matching:"
					+ "\nlocal state:"+localState
					+ "\nreceived:"+request.getParameter("state")
					);
			response.setStatus(404, "The state parameter is not matching");
			return; //wrong state!
		}
		//decode http response content
		ByteArrayOutputStream writer=new ByteArrayOutputStream();
		transferTo(request.getInputStream(),writer);
		String jweString=writer.toString();
		getServletContext().setAttribute(AbstractServlet.ENCRYPTED_CLIENT_KEY,jweString);
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
