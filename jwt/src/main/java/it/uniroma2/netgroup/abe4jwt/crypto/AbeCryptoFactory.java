package it.uniroma2.netgroup.abe4jwt.crypto;

public class AbeCryptoFactory {
	static AbeCryptoProvider __provider;
	
	public static AbeCryptoProvider get() throws Exception {
		return __provider==null?__provider=new AbeCryptoProvider()
				:__provider;
	}
	public static AbeCryptoProvider get(final String authority, final String scheme) throws Exception {
		if ( __provider==null) return __provider=new AbeCryptoProvider(authority,scheme);
		if (!__provider.getAuthority().equals(authority)
				||!__provider.getScheme().equals(scheme)) throw new Exception("Authority or scheme does not match with existing provider."
						+ "\nExisting Provider parameter: Authority:"+__provider.getAuthority()+
						",Scheme:"+__provider.getScheme());
		return __provider;
	}
}
