package it.uniroma2.netgroup.abe4jwt.showcase.client;

import org.eclipse.microprofile.config.Config;

import com.nimbusds.jose.util.Base64;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = "/logoff")
public class LogoffServlet extends AbstractServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String userId=(String) request.getSession().getAttribute(AbstractServlet.SUB);
		if (userId!=null) System.out.println("LOGOFF user "+userId+" has left \n\n\n");
		request.getSession().invalidate();
//		request.getSession().removeAttribute(EPHKEY);
//		request.getSession().removeAttribute(SUB);
//		request.getSession().removeAttribute(SCOPE);
//		request.getSession().removeAttribute(JWT);
		dispatch("/index.jsp", request, response);
	}
}
