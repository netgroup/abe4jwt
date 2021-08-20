package it.uniroma2.netgroup.abe4jwt.test;

import java.awt.geom.GeneralPath;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoProvider;
import it.uniroma2.netgroup.abe4jwt.jose.KPABEDecrypter;
import it.uniroma2.netgroup.abe4jwt.jose.KPABEEncrypter;

/**
 * Hello world!
 *
 */
public class TestPerformance {	
	private static final String APPROVED_SCOPE = "/blog/get/users/mario.rossi@italia.it/posts "
			+ "/blog/add/users "
			+ "/blog/set/users/mario.rossi@italia.it/name "
			+ "/blog/set/users/mario.rossi@italia.it/country "
			+ "/blog/add/users/mario.rossi@italia.it/posts";
	private static final String AUDIENCE = "http://localhost:9999";
	private static final String SUBJECT = "mario.rossi@italia.it";
	private static final String CLIENT_ID = "http://localhost:9180";
	private static final String ISSUER = "http://localhost:9080";
	private static long MAX_TIME_MINUTES=60;

	static AbeCryptoProvider _abeProvider;
	static {
		try {
			_abeProvider = AbeCryptoFactory.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws Exception {
		int threads=0;
		try {
			threads=Integer.parseInt(args[0]);
		} catch (Exception e) { 
			System.out.println("Performance test. Please set the number of threads by writing it at the end of the command, e.g. TestPerformance 1");
			return;
		}
		final String secret=UUID.randomUUID().toString().replaceAll("-", "");
		if (threads<2) {
			printKeyGenLabel();
			long startTime=System.currentTimeMillis();
			for (int i=0;i<500;i++) 
				keyGen(ISSUER, 
						CLIENT_ID,
						SUBJECT,
						AUDIENCE,
						APPROVED_SCOPE, 
						startTime);
			printEncryptLabel();
			startTime=System.currentTimeMillis();
			for (int i=0;i<500;i++) {
				String[] scopes=APPROVED_SCOPE.split(" ");
				encrypt(scopes[(int)((Math.random()*scopes.length))], CLIENT_ID,secret, startTime);
			}
		} else {
			printKeyGenLabel();
			ExecutorService pool = Executors.newFixedThreadPool(threads);
			final long startTime=System.currentTimeMillis();
			for (int i=0;i<500;i++) {
				pool.execute(new Runnable() {
					public void run() {
						keyGen(ISSUER, 
								CLIENT_ID,
								SUBJECT,
								AUDIENCE,
								APPROVED_SCOPE, 
								startTime);
					}
				});
			}
			pool.shutdown();
			pool.awaitTermination(MAX_TIME_MINUTES, TimeUnit.MINUTES);
			printEncryptLabel();
			ExecutorService pool2 = Executors.newFixedThreadPool(threads);
			final long startTime2=System.currentTimeMillis();
			for (int i=0;i<500;i++) {
				pool2.execute(new Runnable() {
					public void run() {
						String[] scopes=APPROVED_SCOPE.split(" ");
						encrypt(scopes[(int)((Math.random()*scopes.length))], CLIENT_ID,secret, startTime);
					}
				});
			}
			pool2.shutdown();
			pool2.awaitTermination(MAX_TIME_MINUTES, TimeUnit.MINUTES);			
		}
	}

	private static void printKeyGenLabel() {
		System.out.println("Elapsed Time Attr_Num Key_Length");
	}

	private static void printEncryptLabel() {
		System.out.println("Elapsed Time Encrypted_Length");
	}

	public static void keyGen(String issuer, String client, String user, String audience, String approvedScope, long startTime) {
		Instant now = Instant.now();
		Date expirationTime = Date.from(now.plus(30L, ChronoUnit.MINUTES));
		//generate ephemeral key
		String[] scopes=approvedScope.split(" ");
		int n=(int)((Math.random()*scopes.length));
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<n;i++) sb.append("|scope:"+scopes[i]);
		try {
			long t0=System.currentTimeMillis();
			Base64URL k=AbeCryptoFactory.get().keyGen("issuer:"+issuer+
					"|user:"+user+
					"|client_id:"+client+
					"|audience:"+audience+
					sb.toString()+
					"|exp:"+expirationTime.getTime());
			long t=System.currentTimeMillis();
			System.out.println(" "+(t-startTime)+" "+(t-t0)+" "+n+" "+k.toString().length());
		} catch (Exception e) {
			System.out.println("Ephemeral key generation failure.");
			e.printStackTrace();
			return;
		}
	}

	public static String encrypt(String resourceId, String clientId, String secret, long startTime) {
		String realm=null;
		if (_abeProvider!=null) {
			try {
				//encrypt secret using: clientId, userId, resourceId, thisServerId, local time (LocalDateTime)
				StringBuffer encKey=new StringBuffer(); //will hold symmetric encryption key
				byte[] plainText=secret.getBytes();
				final String encryptInput="issuer:"+ISSUER+
						" and client_id:"+clientId+
						" and audience:"+AUDIENCE+
						" and scope:"+resourceId+
						" and exp:"+(new SimpleDateFormat("yyyy-MM-dd")).format(Date.from(Instant.now()));  //for the time being, we don't handle time, just assume expiration is today at midnight
				long t0=System.currentTimeMillis();
				Base64URL encrypted=_abeProvider.encrypt(encryptInput, plainText, encKey);
				//then, encrypt once more using: clientID
				StringBuffer encKey2=new StringBuffer(); //will hold simmetric encryption key
				Base64URL encrypted2=_abeProvider.encrypt("client_id:"+clientId, (encKey.toString()+"_"+encrypted.toString()).getBytes(), encKey2);
				realm=encKey2.toString()+"_"+encrypted2.toString();
				long t=System.currentTimeMillis();
				System.out.println(" "+(t-startTime)+" "+(t-t0)+" "+realm.length());
			} catch (Exception e) {
				System.out.println("Encryption failure.");
				e.printStackTrace();
			}
		} else { 
			System.out.println("Encryption failure.");
		}
		return realm;
	}
}
