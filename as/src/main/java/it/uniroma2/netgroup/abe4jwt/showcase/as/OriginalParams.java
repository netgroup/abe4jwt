package it.uniroma2.netgroup.abe4jwt.showcase.as;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
class OriginalParams implements Serializable {
	String userId;
	String clientId;
	String redirectUri;
	String scope;
	String nonce;
	String state;
	
	public OriginalParams() {
		super();
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getRedirectUri() {
		return redirectUri;
	}
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

}
