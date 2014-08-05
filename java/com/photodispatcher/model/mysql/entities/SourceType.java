package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.src_type")
public class SourceType extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="loc_type")
    private int loc_type;
    @Column(name="name")
    private String name;
    @Column(name="state")
    private int state;
    @Column(name="book_part")
    private int book_part;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLoc_type() {
		return loc_type;
	}
	public void setLoc_type(int loc_type) {
		this.loc_type = loc_type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	
}
