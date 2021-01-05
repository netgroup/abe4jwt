package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;

@RequestScoped
@Path("authorize")
public class AuthorizationEndpoint {
	@Inject JWTFactory factory;

	//Retrieve the original request (GET), save parameters in session, if needed route user to login (login.jsp), dispatch to auth form (authorization.jsp).
	//And finally authorize the user (POST).

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doGet(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context UriInfo uriInfo) throws ServletException, IOException {
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		System.out.println("session is: "+request.getSession());

		//user,client,redirect_uri, audience_uri, scope and state received
		String userId=params.getFirst("user"); 
		//if provided and already logged, go to authorization form
		//if provided but not actually logged, go to login form
		//if not provided, go to login form
		String clientId = params.getFirst("client");
		String redirectUri = params.getFirst("redirect_uri");
		try {
			URI temp = UriBuilder.fromUri(params.getFirst("audience_uri")).build();
			//			params.putSingle("audience", temp.getHost()+(temp.getPort()<0?"":":"+temp.getPort()));
		} catch (Exception e) {
			return informUserAboutError(request, response, "Invalid 'audience_uri' :" + params.getFirst("audience_uri"));
		}
		if (clientId == null || clientId.isEmpty()) return informUserAboutError(request, response, "Invalid 'client_id' :" + clientId);
		try {
			if (!redirectUri.startsWith(clientId))
				return informUserAboutError(request, response, "'client' host ["+clientId+"] must match 'redirect_uri' host ["+redirectUri+"]");
		} catch (Exception e) {
			return informUserAboutError(request, response, "'redirect_uri' ["+redirectUri+"] is invalid");
		}		
		String scope = params.getFirst("scope");
		if (scope == null || scope.isEmpty()) 
			return informUserAboutError(request, response, "No 'scope' provided");
		String state = params.getFirst("state");
		if (state == null || state.isEmpty()) 
			return informUserAboutError(request, response, "No 'state' provided");
		if (originalParams!=null
				&&originalParams.getFirst("logged_user")!=null
				&&(userId == null||userId.equals(originalParams.getFirst("logged_user")))) { //user already logged in on a previous request, keep session valid
			System.out.println("User "+originalParams.getFirst("logged_user")+" has a valid session, skipping authentication by mail...");
			params.putSingle("logged_user", originalParams.getFirst("logged_user"));
			params.putSingle("user",originalParams.getFirst("logged_user"));
			params.putSingle("scope", params.getFirst("scope").replaceAll("\\{id\\}", params.getFirst("logged_user")));
			//each request has a reqId which is passed to authorize.jsp, 
			//this way, in the doPost() method below, we're sure the request is a fresh Client request 
			//and does not come from a stale authorization.jsp page. 'State' is not exposed directly but 
			//used to derive a reqId through a hasfunction that cannot be reversed.
			request.setAttribute("reqId", params.getFirst("state").hashCode()); 
			request.getSession().setAttribute("ORIGINAL_PARAMS", params);
			request.getRequestDispatcher("/authorize.jsp").forward(request, response);
		} else {
			//reqId is added after email verification (see EmailVerified.doGet() method)
			request.getSession().setAttribute("ORIGINAL_PARAMS", params);
			request.getRequestDispatcher("/login").forward(request, response);
		}
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response doPost(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context UriInfo uriInfo,
			MultivaluedMap<String, String> params) throws Exception {
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		System.out.println("authorization submitted from user, originalParams: "+originalParams);
		if (originalParams.getFirst("state").hashCode()!=Integer.parseInt(request.getParameter("reqId"))) {//by this check we're sure that the request is not from a stale page authorize.jsp page
			System.out.println("requestId not matching, perhaps a stale request? "+request.getParameter("reqId")+" instead of "+originalParams.getFirst("state").hashCode());
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		if (originalParams==null||originalParams.getFirst("logged_user")==null) {//unauthenticated 
			System.out.println("no originalParams found");
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		String clientId = originalParams.getFirst("client");
		String audience = originalParams.getFirst("audience_uri");
		String userId = originalParams.getFirst("logged_user");
		String redirectUri = originalParams.getFirst("redirect_uri");
		StringBuilder returnUri = new StringBuilder(redirectUri);
		String approvalStatus = params.getFirst("approval_status");
		if ("NO".equals(approvalStatus)) {
			URI location = UriBuilder.fromUri(returnUri.toString())
					.queryParam("error", "User doesn't approved the request.")
					.queryParam("error_description", "User doesn't approved the request.")
					.build();
			return Response.seeOther(location).build();
		}
		//==> YES
		List<String> approvedScopes = params.get("scope");
		if (approvedScopes == null || approvedScopes.isEmpty()) {
			URI location = UriBuilder.fromUri(returnUri.toString())
					.queryParam("error", "User doesn't approved the request.")
					.queryParam("error_description", "User doesn't approved the request.")
					.build();
			return Response.seeOther(location).build();
		}
		final String compared=originalParams.getFirst("scope")+" "; //check whether all scopes from the request are allowed!!
		StringBuffer scopeBuf =new StringBuffer();
		for (String s:approvedScopes) {
			if (compared.indexOf(s+" ")<0) {
				URI location = UriBuilder.fromUri(returnUri.toString())
						.queryParam("error", "Wrong scope approved")
						.queryParam("error_description", "'scope' ["+s+"] was approved by the user, but it was not in the original request")
						.build();
				return Response.seeOther(location).build();				
			}
			scopeBuf.append(s+" ");
		} 

		String issuer=uriInfo.getBaseUri().toString();
		if (issuer.endsWith("/")) issuer=issuer.substring(0,issuer.length()-1);
		String token=factory.create(issuer, 
				clientId, 
				userId, 
				audience, 
				scopeBuf.toString().trim());
		System.out.println("Token generated! "/*+token*/); 
		returnUri.append("?code=").append(token);
		String state = originalParams.getFirst("state");
		if (state != null) {
			returnUri.append("&state=").append(state);
		}
		return Response.seeOther(UriBuilder.fromUri(returnUri.toString()).build()).build();
	}

	private Response informUserAboutError(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException {
		request.setAttribute("error", error);
		request.getRequestDispatcher("/error.jsp").forward(request, response);
		return null;
	}
}
