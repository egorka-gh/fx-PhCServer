package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "form_field_items")
public class PrintFormFieldItem extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="form_field")
    private int form_field;
    @Column(name="sequence")
    private int sequence;
    @Column(name="is_field")
    private boolean is_field;
    @Column(name="child_field")
    private int child_field;
    @Column(name="attr_type")
    private int attr_type;
    @Column(name="delemiter")
    private String delemiter;
    @Column(name="prefix")
    private String prefix;
    @Column(name="sufix")
    private String sufix;
    
    //ref
    @Column(name="property", updatable=false, insertable=false)
    private String property;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getForm_field() {
		return form_field;
	}
	public void setForm_field(int form_field) {
		this.form_field = form_field;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public boolean isIs_field() {
		return is_field;
	}
	public void setIs_field(boolean is_field) {
		this.is_field = is_field;
	}
	public int getChild_field() {
		return child_field;
	}
	public void setChild_field(int child_field) {
		this.child_field = child_field;
	}
	public int getAttr_type() {
		return attr_type;
	}
	public void setAttr_type(int attr_type) {
		this.attr_type = attr_type;
	}
	public String getDelemiter() {
		return delemiter;
	}
	public void setDelemiter(String delemiter) {
		this.delemiter = delemiter;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSufix() {
		return sufix;
	}
	public void setSufix(String sufix) {
		this.sufix = sufix;
	}

    
}
