package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "book_synonym_glue")
public class BookSynonymGlue extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="book_synonym")
    private int book_synonym;
    @Column(name="paper")
    private int paper;
    @Column(name="interlayer")
    private int interlayer;
    @Column(name="glue_cmd")
    private int glue_cmd;
    
	//ref
    @Column(name="paper_name", insertable=false, updatable=false)
    private String paper_name;
    @Column(name="interlayer_name", insertable=false, updatable=false)
    private String interlayer_name;
    @Column(name="glue_cmd_name", insertable=false, updatable=false)
    private String glue_cmd_name;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBook_synonym() {
		return book_synonym;
	}
	public void setBook_synonym(int book_synonym) {
		this.book_synonym = book_synonym;
	}
	public int getPaper() {
		return paper;
	}
	public void setPaper(int paper) {
		this.paper = paper;
	}
	public int getInterlayer() {
		return interlayer;
	}
	public void setInterlayer(int interlayer) {
		this.interlayer = interlayer;
	}
	public int getGlue_cmd() {
		return glue_cmd;
	}
	public void setGlue_cmd(int glue_cmd) {
		this.glue_cmd = glue_cmd;
	}
	public String getPaper_name() {
		return paper_name;
	}
	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}
	public String getInterlayer_name() {
		return interlayer_name;
	}
	public void setInterlayer_name(String interlayer_name) {
		this.interlayer_name = interlayer_name;
	}
	public String getGlue_cmd_name() {
		return glue_cmd_name;
	}
	public void setGlue_cmd_name(String glue_cmd_name) {
		this.glue_cmd_name = glue_cmd_name;
	}


}
