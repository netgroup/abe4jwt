package it.uniroma2.netgroup.abe4jwt.showcase.guestbookserver;

import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

public class MessageResponse {
    private String message;
	
    public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MessageResponse() {
    	super();
    }
    
	public MessageResponse(String message) {
    	super();
    	this.message=message;
    }
	
	public boolean equals(Object o) {
		if (o instanceof MessageResponse&&message!=null&&message.equals(((MessageResponse)o).message)) return true;
		return false;
	}
    
}
