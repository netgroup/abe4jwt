package it.uniroma2.netgroup.abe4jwt.crypto;

public class AbeCryptoFactory {
	static AbeCryptoProvider __provider;
	
	public static AbeCryptoProvider get() throws Exception {
		return __provider==null?__provider=new AbeCryptoProvider()
				:__provider;
	}
	public static AbeCryptoProvider get(final String authority, final String scheme) throws Exception {
		return __provider==null?__provider=new AbeCryptoProvider(authority,scheme)
				:__provider;
	}
}
