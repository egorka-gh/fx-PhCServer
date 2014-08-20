package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcdata.tech_log")
public class TechLog extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="order_id")
    private String order_id="";
    @Column(name="print_group")
    private String print_group="";
    @Column(name="sheet")
    private int sheet;
    @Column(name="src_id")
    private int src_id;
    @Column(name="log_date")
    private Date log_date;

    
	//ref props
    @Column(name="tech_point_name", insertable=false, updatable=false)
    private String tech_point_name;
    @Column(name="tech_state", insertable=false, updatable=false)
    private int tech_state;
    @Column(name="tech_state_name", insertable=false, updatable=false)
    private String tech_state_name;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getPrint_group() {
		return print_group;
	}
	public void setPrint_group(String print_group) {
		this.print_group = print_group;
	}
	public int getSheet() {
		return sheet;
	}
	public void setSheet(int sheet) {
		this.sheet = sheet;
	}
	public int getSrc_id() {
		return src_id;
	}
	public void setSrc_id(int src_id) {
		this.src_id = src_id;
	}
	public Date getLog_date() {
		return log_date;
	}
	public void setLog_date(Date log_date) {
		this.log_date = log_date;
	}
	public String getTech_point_name() {
		return tech_point_name;
	}
	public void setTech_point_name(String tech_point_name) {
		this.tech_point_name = tech_point_name;
	}
	public int getTech_state() {
		return tech_state;
	}
	public void setTech_state(int tech_state) {
		this.tech_state = tech_state;
	}
	public String getTech_state_name() {
		return tech_state_name;
	}
	public void setTech_state_name(String tech_state_name) {
		this.tech_state_name = tech_state_name;
	}

}
