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

@Entity
@Table(name = "USERS")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "name")
    private String name;
    @Column(name = "country")
    private String country;
    @OneToMany(mappedBy = "user", cascade=CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<BlogEntry> blogEntries;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public List<BlogEntry> getBlogEntries() {
		return blogEntries;
	}
	public void setBlogEntries(List<BlogEntry> blogEntries) {
		this.blogEntries = blogEntries;
	}
    public User() {
    	super();
    }
    
	public boolean equals(Object o) {
		if (o instanceof User&&id!=null&&id.equals(((User)o).id)) return true;
		return false;
	}
    
}
