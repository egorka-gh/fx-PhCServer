package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcdata.order_exstate_prolong")
public class OrderExtraStateProlong extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="id")
    private String id;
    @Id
    @Column(name="sub_id")
    private String sub_id;
    @Id
    @Column(name="state_date")
    private Date state_date;
    @Column(name="state")
    private int state;
    @Column(name="comment")
    private String comment;

	//ref
    @Column(name="state_name")
    private String state_name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSub_id() {
		return sub_id;
	}

	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}

	public Date getState_date() {
		return state_date;
	}

	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getState_name() {
		return state_name;
	}

	public void setState_name(String state_name) {
		this.state_name = state_name;
	}

}
