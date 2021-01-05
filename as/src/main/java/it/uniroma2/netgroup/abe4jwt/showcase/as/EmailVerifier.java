package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

//@FormAuthenticationMechanismDefinition(
//		loginToContinue = @LoginToContinue(loginPage = "/login.jsp", errorPage = "/login.jsp")
//		)
//@RolesAllowed("USER")

@RequestScoped
@Path("check")
public class EmailVerifier {
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doGet(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context UriInfo uriInfo) throws ServletException, IOException {
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		String nonce=params.getFirst("nonce");
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		if (originalParams!=null) {
			System.out.println("checking noce, stored nonce is: "+originalParams.getFirst("nonce"));
			if (nonce!=null) {
				System.out.println("checking noce, presented nonce is: "+nonce);
				String user=originalParams.getFirst("user");
				if (nonce.equals(originalParams.getFirst("nonce"))&&user!=null) {
					//bingo! user is authorized
					System.out.println("nonce ok, user "+user+" logged in");
					originalParams.putSingle("logged_user", user);
					String scope = originalParams.getFirst("scope");
					originalParams.putSingle("scope", scope.replaceAll("\\{id\\}", originalParams.getFirst("logged_user")));
					
					//each request has a reqId which is passed to authorize.jsp, 
					//this way, in the doPost() method below, we're sure the request is a fresh Client request 
					//and does not come from a stale authorization.jsp page. 'State' is not exposed directly but 
					//used to derive a reqId through a hasfunction that cannot be reversed.
					request.setAttribute("reqId", originalParams.getFirst("state").hashCode());
					request.getRequestDispatcher("/authorize.jsp").forward(request, response);	
					return null;
				} 
			}
		}
		return informUserAboutError(request, response, "Expired or invalid nonce :" + nonce);
	}



	private Response informUserAboutError(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException {
		request.setAttribute("error", error);
		request.getRequestDispatcher("/error.jsp").forward(request, response);
		return null;
	}
}
