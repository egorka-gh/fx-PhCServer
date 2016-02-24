package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tech_reject_items")
public class TechRejectItems extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="tech_reject")
    private int tech_reject;
    @Column(name="pg_src")
    private String pg_src;
    @Column(name="thech_unit")
    private int thech_unit;
    @Column(name="book")
    private int book;
    @Column(name="sheet")
    private int sheet;
    @Column(name="qty")
    private int qty;
    
    //db drived
    @Column(name="thech_unit_name", updatable=false, insertable=false)
    private String thech_unit_name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTech_reject() {
		return tech_reject;
	}

	public void setTech_reject(int tech_reject) {
		this.tech_reject = tech_reject;
	}

	public String getPg_src() {
		return pg_src;
	}

	public void setPg_src(String pg_src) {
		this.pg_src = pg_src;
	}

	public int getThech_unit() {
		return thech_unit;
	}

	public void setThech_unit(int thech_unit) {
		this.thech_unit = thech_unit;
	}

	public int getBook() {
		return book;
	}

	public void setBook(int book) {
		this.book = book;
	}

	public int getSheet() {
		return sheet;
	}

	public void setSheet(int sheet) {
		this.sheet = sheet;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getThech_unit_name() {
		return thech_unit_name;
	}

	public void setThech_unit_name(String thech_unit_name) {
		this.thech_unit_name = thech_unit_name;
	}


}
