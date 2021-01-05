package it.uniroma2.netgroup.abe4jwt.showcase.guestbookserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.QueryHint;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.validator.EmailValidator;

@ApplicationScoped
@Path("blog")
public class RepositoryEndpoint {
//	HTTP return codes
//	200 OK
//	201 Created
//	202 Accepted
	
	@PersistenceContext
	private EntityManager entityManager;

// Additional methods was implemented to provide an ADMIN interface.
// ADMIN interface
//  	|
//		v
// Although valid, these methods should not be exposed to the proxy, which is designed for end user access only. 
// Of course, it is possible to think at a more sophisticated proxy, offering also an administrator interface
// but for the time being we limited to implemented a basic version. So we leave this code commented for future use.
// 

	
//	//curl "http://localhost:9280/blog/protected/add/users" -d "{\"email\":\"email\"}" -H "Content-Type: application/json"
//	//curl "http://localhost:9280/rs/blog/protected/add/users" -d "{\"email\":\"email\"}" -H "Content-Type: application/json"
//	@Transactional
//	@POST @Path("protected/add/users")
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response addUser(User u) throws Exception {
//		if (!validate(u.getEmail())) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();
//		if (userByEmail(u.getEmail())!=null) return Response.status(202).entity(new MessageResponse("User exists:"+u.getEmail())).build();
//		u.setId(null);
//		entityManager.persist(u);
//		return Response.status(201).entity(u).build();
//	}
//
//	//curl "http://localhost:9280/blog/protected/get/users"
//	//curl "http://localhost:9280/rs/blog/protected/get/users"
//	@GET @Path("protected/get/users")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response listUsers() throws ServletException, IOException {
//		List<User> users = (List<User>) entityManager.createQuery("SELECT u from User u").getResultList();
//		return Response.ok().entity(users).build();
//	}

//
// END-USER interface
// 		  	|
//			v
	
	//depending upon deployment URI assigned to the servlet (i.e., root context / or /rs), use first or second line to test with curl:
	
	//curl "http://localhost:9280/blog/protected/get/users/{email}/profile"
	//curl "http://localhost:9280/rs/blog/protected/get/users/{email}/profile"
	@GET @Path("protected/get/users/{email}/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("email") String e) throws Exception {
		if (!validate(e)) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();		
		User u=userByEmail(e);	
		if (u==null) return Response.status(202).entity(new MessageResponse("User does "+e+" not exist")).build();	
		System.out.println("Retrieved profile for user:"+u);
		//workaround to prevent circular reference (@JsonbTransient annotation in BlogEntry.setUser() seems not working!)
		List<BlogEntry> l=u.getBlogEntries(); if (l!=null) for (BlogEntry b:l) b.setUser(null);
		
		try {
			ResponseBuilder rb;
			rb = Response.ok().entity(u);
			Response r=rb.build();
			System.out.println("Response:"+r.toString());
			return r;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return Response.serverError().build();
	}	

	//curl "http://localhost:9280/blog/protected/set/users/{email}/name" -d "new name" -H "Content-Type: text/plain"
	//curl "http://localhost:9280/rs/blog/protected/set/users/{email}/name" -d "new name" -H "Content-Type: text/plain"
	@Transactional
	@POST @Path("protected/set/users/{email}/name")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setName(@PathParam("email")String e, String name) throws Exception {
		if (!validate(e)) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();		
		if (name==null) return Response.status(202).entity(new MessageResponse("Null data provided")).build();		
		User u=userByEmail(e);
		if (u==null) {
			u=new User();
			u.setEmail(e);
		}
		u.setName(name.trim());
		entityManager.merge(u);
		System.out.println("Set name for user:"+u);
		return Response.ok().entity(u).build();
	}	

	//curl "http://localhost:9280/blog/protected/set/users/{email}/country" -d "new country" -H "Content-Type: text/plain"
	//curl "http://localhost:9280/rs/blog/protected/set/users/{email}/country" -d "new country" -H "Content-Type: text/plain"
	@Transactional
	@POST @Path("protected/set/users/{email}/country")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCountry(@PathParam("email")String e, String country) throws Exception {
		if (!validate(e)) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();		
		if (country==null) return Response.status(202).entity(new MessageResponse("Null data provided")).build();	
		User u=userByEmail(e);
		if (u==null) {
			u=new User();
			u.setEmail(e);
		}		u.setCountry(country.trim());
		entityManager.merge(u);
		System.out.println("Set Country for user:"+u);
		return Response.ok().entity(u).build();
	} 

	//curl "http://localhost:9280/blog/protected/get/users/{email}/posts"
	//curl "http://localhost:9280/rs/blog/protected/get/users/{email}/posts"
	@GET @Path("protected/get/users/{email}/posts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postsByUser(@PathParam("email") String e) throws Exception {
		if (!validate(e)) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();		
		User u=userByEmail(e);
		if (u==null) return Response.status(202).entity(new MessageResponse("User does "+e+" not exist")).build();	
		//workaround to prevent circular reference (@JsonbTransient annotation in BlogEntry.setUser() seems not working!)
		List<BlogEntry> l=u.getBlogEntries();if (l!=null) for (BlogEntry b:l) b.getUser().setBlogEntries(null);
		System.out.println("Got user's post:"+l);
		return Response.ok().entity(l).build();
	}	

	//curl "http://localhost:9280/blog/protected/add/users/{email}/posts" -d "{\"title\":\"new post\"}" -H "Content-Type: application/json"
	//curl "http://localhost:9280/rs/blog/protected/add/users/{email}/posts" -d "{\"title\":\"new post\"}" -H "Content-Type: application/json"
	@Transactional
	@POST @Path("protected/add/users/{email}/posts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response post(@PathParam("email") String e, BlogEntry b) throws Exception {
		if (!validate(e)) return Response.status(202).entity(new MessageResponse("Please supply a valid email address for user.")).build();		
		b.setId(null);
		User u=(User) userByEmail(e);
		if (u==null) {
			u=new User();
			u.setEmail(e);
			entityManager.persist(u);
		}
		if (u.getBlogEntries()==null) u.setBlogEntries(new ArrayList<BlogEntry>());
		u.getBlogEntries().add(b);
		b.setUser(u);
		entityManager.persist(b);
		//workaround to prevent circular reference (@JsonbTransient annotation in BlogEntry.setUser() seems not working!)
		b.getUser().setBlogEntries(null);
		System.out.println("Added user's post:"+b);
		return Response.status(201).entity(b).build();
	}		

	//curl "http://localhost:9280/blog/get/users/posts/latest/{num}"
	//curl "http://localhost:9280/rs/blog/get/users/posts/latest/{num}"
	@GET @Path("get/users/posts/latest/{num}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response latestPosts(@PathParam("num") int num) throws Exception {
		Query q=entityManager.createQuery("SELECT b FROM BlogEntry b ORDER BY b.id DESC").setMaxResults(num);
		List<BlogEntry> posts= (List<BlogEntry>) q.getResultList();
		//workaround to prevent circular reference (@JsonbTransient annotation in BlogEntry.setUser() seems not working!)
		for (BlogEntry b:posts) b.getUser().setBlogEntries(null);
		System.out.println("Got latest "+num+" post:"+posts);
		return Response.ok().entity(posts).build();
	}	  

	private User userByEmail(String email) {
		Query q=entityManager.createQuery("SELECT u from User u "
				+ "WHERE u.email = :email").setParameter("email",email);
		try {
			return (User) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} 
	}

	private boolean validate(String email) {
		//TODO: method is deprecated update this line asap!
		return EmailValidator.getInstance().isValid(email);
	}
}
