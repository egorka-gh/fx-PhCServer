package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "prn_queue_link")
public class PrnQueueLink extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="prn_queue")
    private int prn_queue;
    @Column(name="book_part")
    private int book_part;
    @Column(name="prn_queue_link")
    private int prn_queue_link;
    @Column(name="book_part_link")
    private int book_part_link;

    //db drived
    @Column(name="book_part_name", updatable=false, insertable=false)
    private String book_part_name;
    @Column(name="book_part_link_name", updatable=false, insertable=false)
    private String book_part_link_name;
	public int getPrn_queue() {
		return prn_queue;
	}
	public void setPrn_queue(int prn_queue) {
		this.prn_queue = prn_queue;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	public int getPrn_queue_link() {
		return prn_queue_link;
	}
	public void setPrn_queue_link(int prn_queue_link) {
		this.prn_queue_link = prn_queue_link;
	}
	public int getBook_part_link() {
		return book_part_link;
	}
	public void setBook_part_link(int book_part_link) {
		this.book_part_link = book_part_link;
	}
	public String getBook_part_name() {
		return book_part_name;
	}
	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
	}
	public String getBook_part_link_name() {
		return book_part_link_name;
	}
	public void setBook_part_link_name(String book_part_link_name) {
		this.book_part_link_name = book_part_link_name;
	}

    

}
