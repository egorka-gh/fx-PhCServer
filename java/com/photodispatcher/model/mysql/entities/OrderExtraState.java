package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcdata.order_extra_state")
public class OrderExtraState extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="id")
    private String id;
    @Id
    @Column(name="sub_id")
    private String sub_id;
    @Id
    @Column(name="state")
    private int state;
    @Column(name="start_date")
    private Date start_date;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="reported")
    private boolean reported;
    
    //ref
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    @Column(name="books", updatable=false, insertable=false)
    private int books;
    @Column(name="books_done", updatable=false, insertable=false)
    private int books_done;
    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public Date getState_date() {
		return state_date;
	}
	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}
	public boolean isReported() {
		return reported;
	}
	public void setReported(boolean reported) {
		this.reported = reported;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public int getBooks() {
		return books;
	}
	public void setBooks(int books) {
		this.books = books;
	}
	public int getBooks_done() {
		return books_done;
	}
	public void setBooks_done(int books_done) {
		this.books_done = books_done;
	}

}
