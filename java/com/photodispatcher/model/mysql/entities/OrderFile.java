package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "order_files")
public class OrderFile extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="order_id")
    private String order_id;
    @Id
    @Column(name="file_name")
    private String file_name;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="size")
    private int size;
    @Column(name="hash_remote")
    private String hash_remote;
    @Column(name="hash_local")
    private String hash_local;


    //ref
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    @Column(name="previous_state", updatable=false, insertable=false)
    private int previous_state;
    @Column(name="previous_state_name", updatable=false, insertable=false)
    private String previous_state_name;
    
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
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
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getHash_remote() {
		return hash_remote;
	}
	public void setHash_remote(String hash_remote) {
		this.hash_remote = hash_remote;
	}
	public String getHash_local() {
		return hash_local;
	}
	public void setHash_local(String hash_local) {
		this.hash_local = hash_local;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public int getPrevious_state() {
		return previous_state;
	}
	public void setPrevious_state(int previous_state) {
		this.previous_state = previous_state;
	}
	public String getPrevious_state_name() {
		return previous_state_name;
	}
	public void setPrevious_state_name(String previous_state_name) {
		this.previous_state_name = previous_state_name;
	}


}
