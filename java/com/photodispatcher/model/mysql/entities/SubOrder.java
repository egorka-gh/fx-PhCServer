package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcdata.suborders")
public class SubOrder extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="order_id")
    private String order_id;
    @Id
    @Column(name="sub_id")
    private String sub_id;
    /*
    @Column(name="id") //old combo id
    private String id;
    */
    @Column(name="src_type")
    private int src_type;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="ftp_folder")
    private String ftp_folder;
    @Column(name="prt_qty")
    private int prt_qty;
    @Column(name="proj_type")
    private int proj_type;
    
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Date getState_date() {
		return state_date;
	}
	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}
	public String getFtp_folder() {
		return ftp_folder;
	}
	public void setFtp_folder(String ftp_folder) {
		this.ftp_folder = ftp_folder;
	}
	public int getPrt_qty() {
		return prt_qty;
	}
	public void setPrt_qty(int prt_qty) {
		this.prt_qty = prt_qty;
	}
	public int getProj_type() {
		return proj_type;
	}
	public void setProj_type(int proj_type) {
		this.proj_type = proj_type;
	}

}
