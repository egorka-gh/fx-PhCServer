package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "package_prop")
public class MailPackageProperty extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="property")
    private String property;
    @Column(name="value")
    private String value;

    //ref
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="property_name", updatable=false, insertable=false)
    private String property_name;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	public String getProperty_name() {
		return property_name;
	}
	public void setProperty_name(String property_name) {
		this.property_name = property_name;
	}

    
}
