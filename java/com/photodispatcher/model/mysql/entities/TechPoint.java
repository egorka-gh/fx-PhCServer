package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.tech_point")
public class TechPoint extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="tech_type")
    private int tech_type;
    @Column(name="name")
    private String name;

	//db drived
    @Column(name="tech_type_name", updatable=false, insertable=false)
    private String tech_type_name;
    @Column(name="tech_book_part", updatable=false, insertable=false)
    private int tech_book_part;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTech_type() {
		return tech_type;
	}
	public void setTech_type(int tech_type) {
		this.tech_type = tech_type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTech_type_name() {
		return tech_type_name;
	}
	public void setTech_type_name(String tech_type_name) {
		this.tech_type_name = tech_type_name;
	}
	public int getTech_book_part() {
		return tech_book_part;
	}
	public void setTech_book_part(int tech_book_part) {
		this.tech_book_part = tech_book_part;
	}

}
