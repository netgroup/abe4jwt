package it.uniroma2.netgroup.abe4jwt.showcase.as;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("logoff")
public class LogoffEndpoint {

	@POST
	public Response doPost(@Context HttpServletRequest request,
			@Context HttpServletResponse response) throws Exception {
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		if (originalParams!=null) System.out.println("[LogoffEndpoint] user "+originalParams.getFirst("logged_user")+" has left");
		request.getSession().invalidate();
		request.setAttribute("approval_status", "NO");
		request.getRequestDispatcher("/authorize").forward(request, response);
		return null;
	}
}

