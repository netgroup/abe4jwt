package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.validator.EmailValidator;
import org.eclipse.microprofile.config.Config;

import it.uniroma2.netgroup.abe4jwt.util.RandomString;

//import com.baeldung.oauth2.authorization.server.model.AppDataRepository;

@RequestScoped
@Path("login")
public class LoginEndpoint {

	@Inject
	private Config config; //config properties are taken from /src/main/resources/META-INF/microprofile-config.properties

	static RandomString random=new RandomString();

	@GET
	public Response doGet(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context UriInfo uriInfo) throws ServletException, IOException {
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		if (originalParams==null) {
			request.getRequestDispatcher("/authorize").forward(request, response); //error!
			return null;
		}
		String nonce=random.nextString();
		System.out.println("[LoginEndpoint] No user session found. Go to login page, generating nonce... "+nonce);
		originalParams.putSingle("nonce",nonce);
		originalParams.putSingle("uri", uriInfo.getAbsolutePathBuilder().queryParam("nonce", nonce).build().toASCIIString().replace("/login", "/check"));
		request.setAttribute("loginMessage", "Please insert your email address. You'll receive an email message containing a link. "
				+ "To authenticate, click on the link.");
		request.getRequestDispatcher("/login.jsp").forward(request, response);
		return null;
	}
			
	//Get user's email address from the webform and send an email to her in order to check identity
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response doPost(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			MultivaluedMap<String, String> params) throws Exception {
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		if (originalParams==null) {
			request.getRequestDispatcher("/authenticate").forward(request, response);
			return null;
		} 
		if (originalParams.getFirst("captcha")!=null&&!originalParams.getFirst("captcha").equals(params.getFirst("captcha"))) {
			request.setAttribute("loginMessage", "Invalid captcha, please retry.");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return null;			
		};
		String to = params.getFirst("user");
		if (!validate(to)) {
			request.setAttribute("loginMessage", "Please provide a valid email address.");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return null;
		} 
		originalParams.putSingle("user",to);
		//config properties are taken from /src/main/resources/META-INF/microprofile-config.properties
		final String link="<br><a href='"+originalParams.getFirst("uri")+"'>"+originalParams.getFirst("uri")+"</a>";
		final String _ERROR="No API key, no email sent. In production mode, "+
				"please Configure /src/main/resources/META-INF/microprofile-config.properties, "+
				"also consider adding some anti-DoS feature such as <a href='https://www.google.com/recaptcha/about/'>recaptcha</a>"+
				"<h3>REMOVE FROM THIS LINE BELOW IN PRODUCTION CODE</h3><br/>";
		final String apiKey, host, path, from,subject, message;
		try {
			apiKey = config.getValue("mail.token",String.class); 
			host = config.getValue("mail.host",String.class); //https://api.sendgrid.com
			path = config.getValue("mail.path",String.class); //v3/mail/send
			from = config.getValue("mail.from",String.class);
			subject = config.getValue("mail.subject",String.class);
			message = config.getValue("mail.message",String.class);
			//TODO: quirky json encoded string... provide a replacement for this!
			String json= "{\"personalizations\":[{\"to\":[{\"email\":\""+to+"\"}],\"subject\":\""+subject+"\"}],\"content\": [{\"type\": \"text/html\", \"value\": \""+link+"\"}],\"from\":{\"email\":\""+from+"\"}}";
			System.out.println("Logging in params:"+originalParams);	
			if (apiKey!=null&&!apiKey.isEmpty()) {
				Client client = ClientBuilder.newClient();
				WebTarget resourceWebTarget = client.target(host).path(path);
				Invocation.Builder invocationBuilder = resourceWebTarget.request()
						.header("Authorization", "Bearer "+apiKey)
						.header("Content-Type", "application/json");
				Response r=invocationBuilder.post(Entity.entity(json, MediaType.APPLICATION_JSON));
				String resEntity="[no data returned]";
				try {
					resEntity=r.readEntity(String.class);
				} catch (Exception e) {
				}
				System.out.println("Sent POST request to "+resourceWebTarget.getUri()+
						"\n"+json+
						"\nReceived response:"+r.getStatus()+"\n"+resEntity);
				request.setAttribute("loginMessage", "An authorization code has been sent to "+to+".<br/>"+
						"Please check your email.");
			} else {
				request.setAttribute("loginMessage", _ERROR+message);
			}
		} catch (NoSuchElementException e) {
			request.setAttribute("loginMessage", _ERROR+link);
		}
		request.getRequestDispatcher("/login.jsp").forward(request, response);
		return null;
	}

	private boolean validate(String email) {
		//TODO: method is deprecated update this line asap!
		return EmailValidator.getInstance().isValid(email);
	}	
}

