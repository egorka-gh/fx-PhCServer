package com.photodispatcher.model.mysql.entities.report;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

@Table(name="parameter")
public class Parameter extends AbstractEntity{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;

    @Column(name="src_type")
    private int src_type;
    
    @Column(name="name")
    private String name;
    
    @Transient
    private Date valFrom;

    @Transient
    private Date valTo;

    @Transient
    private Date valDate;

    @Transient
    private int valInt;

    @Transient
    private String valString;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;

	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public Date getValFrom() {
		return valFrom;
	}
	public void setValFrom(Date valFrom) {
		this.valFrom = valFrom;
	}
	public Date getValTo() {
		return valTo;
	}
	public void setValTo(Date valTo) {
		this.valTo = valTo;
	}
	public Date getValDate() {
		return valDate;
	}
	public void setValDate(Date valDate) {
		this.valDate = valDate;
	}
	public int getValInt() {
		return valInt;
	}
	public void setValInt(int valInt) {
		this.valInt = valInt;
	}
	public String getValString() {
		return valString;
	}
	public void setValString(String valString) {
		this.valString = valString;
	}
	
}
