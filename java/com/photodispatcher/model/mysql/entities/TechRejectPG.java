package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tech_reject_pg")
public class TechRejectPG extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="tech_reject")
    private int tech_reject;
    @Column(name="pg_src")
    private String pg_src;
    @Column(name="pg_dst")
    private String pg_dst;

    
    /*
    @Column(name="order_id", updatable=false, insertable=false)
    private String order_id;
    @Column(name="sub_id", updatable=false, insertable=false)
    private String sub_id;
    */

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
	public String getPg_dst() {
		return pg_dst;
	}
	public void setPg_dst(String pg_dst) {
		this.pg_dst = pg_dst;
	}
	
}
