package it.uniroma2.netgroup.abe4jwt.showcase.as;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class FakeHostnameVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}

}
