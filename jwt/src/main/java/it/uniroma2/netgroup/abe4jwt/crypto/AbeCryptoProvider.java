

package it.uniroma2.netgroup.abe4jwt.crypto;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.regex.Pattern;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWECryptoParts;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.impl.DeflateHelper;
import com.nimbusds.jose.util.Base64URL;


public class AbeCryptoProvider {

	public String getAuthority() {
		return __authority;
	}

	public String getScheme() {
		return __scheme;
	}

	private final String __authority,__scheme;

	public AbeCryptoProvider() throws Exception {
		this("example.org","CP");
	}

	public AbeCryptoProvider(final String authority, final String scheme) throws Exception {
		__authority=authority;
		__scheme=scheme.toUpperCase();
		if (!"KP".equals(__scheme)&&!"CP".equals(__scheme)) throw new Exception("Unsupported ABE scheme "+__scheme);
		try {
			exec("oabe_setup -s \""+__scheme+"\" -p \""+__authority+"\"");
		} catch (Exception e) {
			System.out.println("WARNING: no ABE cryptosystem found, using dummy");
		}
	}

	synchronized public Base64URL getMPK() throws Exception {
		String out=exec("cat \""+__authority+".mpk."+__scheme.toLowerCase()+"abe\"");
		return new Base64URL(out.split("\n")[1]); 
//		return new Base64URL(exec("cat \""+__authority+".mpk."+__scheme+"abe\"")
//				.replaceAll("(-+BEGIN.*-+\\n)([^\\n]*)(\\n-+END.*-+)","$2")); //encoding regex expr: (-+BEGIN.*-+\n)([^\n]*)(\n-+END.*-+)
	}

	synchronized public void setMPK(Base64URL mpk) throws Exception {
		String out="-----BEGIN MASTER PUBLIC KEY BLOCK-----\n"
				+mpk.decodeToString()+"\n"
				+"-----END MASTER PUBLIC KEY BLOCK-----";
		exec("rm \""+__authority+".mpk."+__scheme.toLowerCase()+"abe\";\n"
				+ "rm \""+__authority+".msk."+__scheme.toLowerCase()+"abe\";\n"
				+ "echo \""+out+"\">\""+__authority+".mpk."+__scheme.toLowerCase()+"abe\"");
	}
	
	synchronized public String getMPKasPEM() throws Exception {
		return exec("cat \""+__authority+".mpk."+__scheme.toLowerCase()+"abe\"");
	}

	synchronized public void setMPKfromPEM(String pemKey) throws Exception {
		exec("rm \""+__authority+".mpk."+__scheme.toLowerCase()+"abe\";\n"
				+ "rm \""+__authority+".msk."+__scheme.toLowerCase()+"abe\";\n"
				+ "echo \""+pemKey+"\">\""+__authority+".mpk."+__scheme.toLowerCase()+"abe\"");
	}

	synchronized public Base64URL keyGen(final String input) throws Exception {
		//oabe_keygen -s SCHEME -p AUTHORITY -i input -o output
		//input: attribute list for 'CP', policy for 'KP'
		String id=UUID.randomUUID().toString(),
				output=exec("oabe_keygen -s \""+__scheme+"\" -p \""+__authority+"\" -i \""+input+"\" -o \""+id+"\" >null\\\n"
						+ "&&cat \""+id+".key\"");
		return new Base64URL(output.split("\n")[1]);
//		return new Base64URL(output.replaceAll("(-+BEGIN.*-+\\n)([^\\n]*)(\\n-+END.*-+)","$2"));  //encoding regex expr: (-+BEGIN.*-+\n)([^\n]*)(\n-+END.*-+)
	}

	synchronized public Base64URL encrypt(final String encryptInput, final byte[] plainText, StringBuffer encKey) throws Exception {
		//oabe_enc -s SCHEME -p AUTHORITY -e encrypt_input -i plaintext -o ciphertext
		
		String id=UUID.randomUUID().toString();
		Base64URL plainBase64=Base64URL.encode(plainText);
		String output=exec("echo \""+plainBase64+"\">\""+id+".txt\"&&\\\n"
				+ "oabe_enc -s \""+__scheme+"\" -p \""+__authority+"\" -e \""+encryptInput+"\" -i \""+id+".txt\" -o \""+id+"."+__scheme.toLowerCase()+"abe\" >null\\\n"
				+ "&&cat \""+id+"."+__scheme.toLowerCase()+"abe\"");
//		//encoding regex expr: (-+BEGIN ABE.*-+\n)([^\n]*)(\n-+END ABE.*-+)(\n-+BEGIN CIPHER.*-+\n)([^\n]*)(\n-+END CIPHER.*-+.*)
//		Pattern p=Pattern.compile("(-+BEGIN ABE.*-+\\n)([^\\n]*)(\\n-+END ABE.*-+)(\\n-+BEGIN CIPHER.*-+\\n)([^\\n]*)(\\n-+END CIPHER.*-+.*)"); 
		String[] str=output.split("\n");
		encKey.append(str[1]);
		return new Base64URL(str[4]);
	}

	synchronized public byte[] decrypt(final Base64URL privateKey, Base64URL encKey, Base64URL cipherText) throws Exception {
		//oabe_dec -s SCHEME -p AUTHORITY -k encrypt_input -i plaintext -o ciphertext
		String key="-----BEGIN USER PRIVATE KEY BLOCK-----\n"
				+privateKey.toString()+"\n"
				+"-----END USER PRIVATE KEY BLOCK-----",
				block="-----BEGIN ABE CIPHERTEXT BLOCK-----\n"+
						encKey.toString()+"\n"+
						"-----END ABE CIPHERTEXT BLOCK-----\n"+
						"-----BEGIN CIPHERTEXT BLOCK-----\n"+
						cipherText.toString()+"\n"+
						"-----END CIPHERTEXT BLOCK-----",
				id=UUID.randomUUID().toString(),
				output=exec("echo \""+key+"\">\""+id+".key\"&&\\\n"
						+ "echo \""+block+"\">\""+id+"."+__scheme.toLowerCase()+"abe\"&&\\\n"
						+ "oabe_dec -s \""+__scheme+"\" -p \""+__authority+"\" -k \""+id+".key\" -i \""+id+"."+__scheme.toLowerCase()+"abe\" -o \""+id+".txt\" >null\\\n"
						+ "&&cat \""+id+".txt\"");
		return new Base64URL(output).decode();
	}	

	public JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText, final String encryptInput) throws JOSEException {
		final byte[] plainText = DeflateHelper.applyCompression(header, clearText);
		StringBuffer encKey=new StringBuffer();
		try {
		Base64URL enc=encrypt(encryptInput,plainText,encKey);
			return new JWECryptoParts(
					header,
					new Base64URL(encKey.toString()),
					null,
					enc,
					null);
		} catch (Exception e) {
			//fake
			return new JWECryptoParts(
					header,
					null,
					null,
					Base64URL.encode(plainText),
					null);
		}
	}

	public byte[] decrypt(final JWEHeader header, final Base64URL privateKey, final Base64URL encKey, final Base64URL cipherText) throws JOSEException {
		byte[] plainText;
		try {
			plainText = decrypt(privateKey,encKey, cipherText);
		} catch (Exception e) {
			//fake
			plainText=cipherText.decode();
		}
		return DeflateHelper.applyDecompression(header, plainText);
	}

	public String exec(String command) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		// -- Linux --
		// Run a shell command
		processBuilder.command("bash", "-c", command);
		// Run a shell script
		//processBuilder.command("path/to/hello.sh");
		// -- Windows --
		// Run a command
		//processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");
		// Run a bat file
		//processBuilder.command("C:\\Users\\mkyong\\hello.bat");
		Process process = processBuilder.start();
		StringBuilder output = new StringBuilder(),
				error=new StringBuilder();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())),
				errorReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}
		while ((line = errorReader.readLine()) != null) {
			error.append(line + "\n");
		}
		int exitVal = process.waitFor();
		final String message;
		if (exitVal != 0) {
			message="code = "+exitVal+": "+error.toString();
//			System.out.println("CMD EXECUTING: "+command+"\nCMD ERROR: "+message);
			throw new Exception(message);
		}
		message=output.toString();
//		System.out.println("CMD EXECUTING: "+command+"\nCMD OUTPUT: "+message);
		return message;
	}
}
