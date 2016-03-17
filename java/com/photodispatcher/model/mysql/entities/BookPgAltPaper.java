package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "book_pg_alt_paper")
public class BookPgAltPaper extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="template")
    private int template;
    @Column(name="sh_from")
    private int sh_from;
    @Column(name="sh_to")
    private int sh_to;
    @Column(name="paper")
    private int paper;
    @Column(name="interlayer")
    private int interlayer;
    @Column(name="revers")
    private boolean revers=false;

    
    //ref name
    @Column(name="paper_name", updatable=false, insertable=false)
    private String paper_name;
    @Column(name="interlayer_name", updatable=false, insertable=false)
    private String interlayer_name;

    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTemplate() {
		return template;
	}
	public void setTemplate(int template) {
		this.template = template;
	}
	public int getSh_from() {
		return sh_from;
	}
	public void setSh_from(int sh_from) {
		this.sh_from = sh_from;
	}
	public int getSh_to() {
		return sh_to;
	}
	public void setSh_to(int sh_to) {
		this.sh_to = sh_to;
	}
	public int getPaper() {
		return paper;
	}
	public void setPaper(int paper) {
		this.paper = paper;
	}
	public String getPaper_name() {
		return paper_name;
	}
	public void setPaper_name(String paper_name) {
		this.paper_name = paper_name;
	}
	public int getInterlayer() {
		return interlayer;
	}
	public void setInterlayer(int interlayer) {
		this.interlayer = interlayer;
	}
	public String getInterlayer_name() {
		return interlayer_name;
	}
	public void setInterlayer_name(String interlayer_name) {
		this.interlayer_name = interlayer_name;
	}
	public boolean isRevers() {
		return revers;
	}
	public void setRevers(boolean revers) {
		this.revers = revers;
	}
}
