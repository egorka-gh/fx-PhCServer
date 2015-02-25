package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "form_field")
public class PrintFormField extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    @Column(name="parametr")
    private String parametr;
    @Column(name="simplex")
    private boolean simplex;
    
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
	public String getParametr() {
		return parametr;
	}
	public void setParametr(String parametr) {
		this.parametr = parametr;
	}
	public boolean isSimplex() {
		return simplex;
	}
	public void setSimplex(boolean simplex) {
		this.simplex = simplex;
	}

}
