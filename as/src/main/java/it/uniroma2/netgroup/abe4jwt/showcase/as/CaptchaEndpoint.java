package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.io.ByteArrayOutputStream;

import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import jj.play.ns.nl.captcha.Captcha;
import jj.play.ns.nl.captcha.backgrounds.GradiatedBackgroundProducer;
import jj.play.ns.nl.captcha.noise.CurvedLineNoiseProducer;

@RequestScoped
@Path("captcha")
public class CaptchaEndpoint {

	static CurvedLineNoiseProducer np=new CurvedLineNoiseProducer();
	static GradiatedBackgroundProducer bp=new GradiatedBackgroundProducer();
	
	//Prepare login form: generate Captcha
	@GET
	@Produces("image/*")
	public Response doGet(@Context HttpServletRequest request,
			@Context HttpServletResponse response) throws Exception {
		MultivaluedMap<String, String> originalParams = (MultivaluedMap<String, String>) request.getSession().getAttribute("ORIGINAL_PARAMS");
		if (originalParams==null) {
			request.getRequestDispatcher("/authenticate").forward(request, response);
			return null;
		}
		Captcha captcha = new Captcha.Builder(200, 50).addText().addBackground(bp).addNoise(np).addBorder().build();
		ByteArrayOutputStream b=new ByteArrayOutputStream();
		ImageIO.write(captcha.getImage(), "PNG", b);
		originalParams.putSingle("captcha",captcha.getAnswer());
		return Response.ok(b.toByteArray(), "image/png").build();
	}
}

