package it.uniroma2.netgroup.abe4jwt.showcase.guestbookserver;

import java.util.Date;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
 

@Entity
@Table(name = "BLOGS")
public class BlogEntry {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="user_fk")
    @JsonbTransient private User user;
    @Column(name = "title")
    private String title;
    @Column(name = "date")
    private Date date;
    @Column(name = "text")
    private String text;


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	//see https://stackoverflow.com/questions/54087178/circular-reference-issue-with-json-b for more details
	@JsonbTransient public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
    public BlogEntry() {
    	super();
    }

	public boolean equals(Object o) {
		if (o instanceof User&&id!=null&&id.equals(((BlogEntry)o).id)) return true;
		return false;
	}
}
