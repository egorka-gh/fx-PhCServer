package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.order_state")
public class OrderState extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	//database props
    @Id
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    @Column(name="runtime")
    private boolean runtime;
    @Column(name="extra")
    private boolean extra;
    @Column(name="tech")
    private boolean tech;
    @Column(name="book_part")
    private int book_part;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isRuntime() {
		return runtime;
	}
	public void setRuntime(boolean runtime) {
		this.runtime = runtime;
	}
	public boolean isExtra() {
		return extra;
	}
	public void setExtra(boolean extra) {
		this.extra = extra;
	}
	public boolean isTech() {
		return tech;
	}
	public void setTech(boolean tech) {
		this.tech = tech;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
    
    


}
