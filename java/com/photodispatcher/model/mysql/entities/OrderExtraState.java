package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "order_extra_state")
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

    //tech monitor 
    @Column(name="state2", updatable=false, insertable=false)
    private int state2;
    @Column(name="start_date2", updatable=false, insertable=false)
    private Date start_date2;
    @Column(name="state_date2", updatable=false, insertable=false)
    private Date state_date2;
    @Column(name="state_name2", updatable=false, insertable=false)
    private String state_name2;

    
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
	public int getState2() {
		return state2;
	}
	public void setState2(int state2) {
		this.state2 = state2;
	}
	public Date getStart_date2() {
		return start_date2;
	}
	public void setStart_date2(Date start_date2) {
		this.start_date2 = start_date2;
	}
	public Date getState_date2() {
		return state_date2;
	}
	public void setState_date2(Date state_date2) {
		this.state_date2 = state_date2;
	}
	public String getState_name2() {
		return state_name2;
	}
	public void setState_name2(String state_name2) {
		this.state_name2 = state_name2;
	}

}
