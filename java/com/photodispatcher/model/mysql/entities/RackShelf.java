package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "rack_shelf")
public class RackShelf extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="rack")
    private int rack;
    @Column(name="name")
    private String name;
    
    //ref
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRack() {
		return rack;
	}

	public void setRack(int rack) {
		this.rack = rack;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRack_name() {
		return rack_name;
	}

	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}

}
