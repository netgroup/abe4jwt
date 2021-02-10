package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import javax.enterprise.inject.Model;
import javax.inject.Named;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.jose.KPABEEncrypter;
import it.uniroma2.netgroup.abe4jwt.util.StringReplacer;

//@Model 
//either use @Model or Change the bean discovery mode in your bean archive from the default "annotated" to "all" by adding a beans.xml file in WEB-INF.
public class JWTFactory {
//public abstract class AbstractGrantTypeHandler implements AuthorizationGrantTypeHandler {

	public JWTFactory() {
		super();
	}
	
    //Always RSA 256, but could be parametrized
    protected JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build();

    public String create(String issuer, String client, String user, String audience, String approvedScope) throws Exception {
        Instant now = Instant.now();
        Date t=Date.from(now);
        Date expirationTime = Date.from(now); //for the time being, we don't handle time, just assume expiration is today at midnight
        expirationTime.setHours(23);
        expirationTime.setMinutes(59);
        expirationTime.setSeconds(59);
        Base64URL ephkey=ephkey(issuer, client, user, audience, approvedScope, expirationTime);
        
        System.out.println("Generating token from:\nissuer:"+issuer+"\n"
        		+ "subject:"+user+"\n"
        		+ "client_id:"+client+"\n"
        		+ "audience:"+audience+"\n"
        		+ "scope:"+approvedScope+"\n"
        		+ "expirationTime:"+expirationTime+"\n"
        		+ "notBeforeTime:"+t+"\n"
        		+ "issueTime:"+t+"\n"
        		+ "ephkey:"+ephkey);
        //from the original RFC 7519 definition, except where otherwise specified
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
        		.issuer(issuer)
        		.subject(user)
        		.claim("client_id", client)  //RFC8693
        		.audience(audience)
        		.claim("scope", approvedScope) //RFC8693
        		.expirationTime(expirationTime) //expires in 30 minutes
        		.notBeforeTime(t)
        		.issueTime(t)
        		.claim("ephkey", ephkey)  //kp-abe ephemeral key
        		.build();
		// Create the encrypted JWT object
		KPABEEncrypter encrypter=new KPABEEncrypter("client_id:"+client);
		JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.parse("KP-ABE"),encrypter.supportedEncryptionMethods().iterator().next()).build();
//		System.out.println("Header:"+header.toString());
//		System.out.println("JWT:"+jwtClaims.toJSONObject().toJSONString());
//		System.out.println("encrypting JWT...");
		EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);       
		jwt.encrypt(encrypter);
		String jwtString=jwt.serialize();
//		System.out.println("Encrypted JWT:"+jwtString);
        return jwtString;
		//Parse back
//		System.out.println("generating client decryption key...");
//		Base64URL clientKey;
//		try {
//			clientKey=AbeCryptoFactory.get().keyGen("client_id:"+client);
//			System.out.println("Client key:"+clientKey.toString());
//		} catch (Exception e) {
//			System.out.println("Client decryption key generation failure.");
//			clientKey=Base64URL.encode("F A K E - K E Y");
//		}
//		System.out.println("decrypting JWT...");
//		EncryptedJWT jwt2 = EncryptedJWT.parse(jwtString);
//		System.out.println("Decrypting JWT:"+jwt2.getParsedString());
//		jwt2.decrypt(new KPABEDecrypter(clientKey));
//		System.out.println("Decrypted JWT:"+jwt.getPayload().toString());
    }
    
    private Base64URL ephkey(String issuer, String client, String user, String audience, String approvedScope, Date expirationTime) {
		if (approvedScope==null) return null;
    	Base64URL ephkey;
		String[] scopes=approvedScope.split(" ");
		StringBuilder sb=new StringBuilder("(scope:"+StringReplacer.replace(scopes[0])); //normalizing scope, look at AbeProxy.generateRealm()
		for (int i=1;i<scopes.length;i++) sb.append(" or scope:"+StringReplacer.replace(scopes[i]));  //normalizing scope, look at AbeProxy.generateRealm()
		
		try {
			String keyString = "issuer:"+issuer+
//					" and user:"+user+ //not used as it is part of the resourceId (specified in scope)
					" and client_id:"+client+
					" and audience:"+audience+
					" and "+sb.append(")").toString()+
					" and exp:"+(new SimpleDateFormat("yyyy-MM-dd")).format(expirationTime);
			System.out.println("---------------\n"
					+ "--> GENERATING EPHEMERAL KEY FROM POLICY:\n"+keyString+"\n"
							+ "\n---------------");
			ephkey=AbeCryptoFactory.get().keyGen(keyString);  //for the time being, we don't handle time, just assume expiration is today at midnight
//			System.out.println("Ephemeral key:"+ephkey.toString());
		} catch (Exception e) {
			System.out.println("Ephemeral key generation failure.");
			return null;
		}
		return ephkey;
    }

}
