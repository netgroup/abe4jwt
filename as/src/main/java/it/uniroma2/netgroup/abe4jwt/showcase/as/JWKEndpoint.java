package it.uniroma2.netgroup.abe4jwt.showcase.as;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;


@ApplicationScoped
@Path("jwk")
public class JWKEndpoint {

	@Inject
	private Config config;

	@GET
	public Response getKey(@QueryParam("format") String format) {
		try {
			String pemEncodedRSAPublicKey = AbeCryptoFactory.get().getMPKasPEM();
			System.out.println("MPK requested from a client:\n"+pemEncodedRSAPublicKey); 
			if (format!=null&&format.equals("jwk")) {
				return Response.serverError().entity("Sorry, JWK format not yet supported...").build();
			} else {
				return Response.ok(pemEncodedRSAPublicKey).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity("ABEProvider error: "+e.getMessage()).build();
		}
	}
}
