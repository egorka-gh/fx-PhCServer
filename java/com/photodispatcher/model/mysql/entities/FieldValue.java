package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;

public class FieldValue extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Column(name="field")
	private String field;
    @Column(name="value")
	private int value;
    @Column(name="label")
	private String label;
	
    @Column(name="src_type")
	private int src_type;
    @Column(name="synonym")
	private String synonym;
    
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public String getSynonym() {
		return synonym;
	}
	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}
    

}
