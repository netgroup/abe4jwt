package it.uniroma2.netgroup.abe4jwt.showcase.client;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

public abstract class AbstractServlet extends HttpServlet {

    public static final String EPHKEY = "ephkey";
	public static final String CLIENT_LOCAL_STATE = "clientLocalState";
	public static final String CLIENT_KEY = "clientKey";
	public static final String SCOPE = "scope";
	public static final String SUB = "sub";
	public static final String JWT = "jwt";

	protected void dispatch(String location, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(location);
        requestDispatcher.forward(request, response);
    }

    protected String getAuthorizationHeaderValue(String clientId, String clientSecret) {
        String token = clientId + ":" + clientSecret;
        String encodedString = Base64.getEncoder().encodeToString(token.getBytes());
        return "Basic " + encodedString;
    }
}
