package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "form_parametr")
public class PrintFormParametr extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="form")
    private int form;
    @Id
    @Column(name="form_field")
    private int form_field;

    //ref
    @Column(name="parametr", updatable=false, insertable=false)
    private String parametr;
    @Column(name="simplex", updatable=false, insertable=false)
    private boolean simplex;

    
	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public int getForm_field() {
		return form_field;
	}

	public void setForm_field(int form_field) {
		this.form_field = form_field;
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
