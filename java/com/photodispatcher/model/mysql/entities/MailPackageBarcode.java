package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "package_prop")
public class MailPackageBarcode extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="barcode")
    private String barcode;
    @Column(name="bar_type")
    private int bar_type;
    
    
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
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public int getBar_type() {
		return bar_type;
	}
	public void setBar_type(int bar_type) {
		this.bar_type = bar_type;
	}
}
