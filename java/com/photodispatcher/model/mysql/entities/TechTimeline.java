package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tech_timeline")
public class TechTimeline extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="tech_process")
    private int tech_process;
    @Id
    @Column(name="tech_type")
    private int tech_type;

    @Column(name="oper_time")
    private int oper_time;
    @Column(name="pass_time")
    private int pass_time;

	//ref
    @Column(name="tech_type_name", insertable=false, updatable=false)
    private String tech_type_name;
    @Column(name="book_part_name", insertable=false, updatable=false)
    private String book_part_name;
    @Column(name="book_part", insertable=false, updatable=false)
    private int book_part;
    
	public int getTech_process() {
		return tech_process;
	}
	public void setTech_process(int tech_process) {
		this.tech_process = tech_process;
	}
	public int getTech_type() {
		return tech_type;
	}
	public void setTech_type(int tech_type) {
		this.tech_type = tech_type;
	}
	public int getOper_time() {
		return oper_time;
	}
	public void setOper_time(int oper_time) {
		this.oper_time = oper_time;
	}
	public int getPass_time() {
		return pass_time;
	}
	public void setPass_time(int pass_time) {
		this.pass_time = pass_time;
	}
	public String getTech_type_name() {
		return tech_type_name;
	}
	public void setTech_type_name(String tech_type_name) {
		this.tech_type_name = tech_type_name;
	}
	public String getBook_part_name() {
		return book_part_name;
	}
	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
    

}
