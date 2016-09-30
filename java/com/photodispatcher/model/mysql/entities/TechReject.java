package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "tech_reject")
public class TechReject extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    @Column(name="created")
    private Date created;
    @Column(name="order_id")
    private String order_id;
    @Column(name="sub_id")
    private String sub_id;
    @Column(name="reject_unit")
    private int reject_unit;
    @Column(name="book")
    private int book;
    @Column(name="activity")
    private int activity;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    
    //db drived
    @Column(name="reject_unit_name", updatable=false, insertable=false)
    private String reject_unit_name;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    @Column(name="sa_type_name", updatable=false, insertable=false)
    private String sa_type_name;
    @Column(name="staff_name", updatable=false, insertable=false)
    private String staff_name;
    @Column(name="sa_remark", updatable=false, insertable=false)
    private String sa_remark;


    //childs
    @Transient
    private List<TechRejectItem> items;
    @Transient
    private List<TechRejectPG> pgroups;
    @Transient
    private StaffActivity activityObj;

    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
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
	public int getReject_unit() {
		return reject_unit;
	}
	public void setReject_unit(int reject_unit) {
		this.reject_unit = reject_unit;
	}
	public int getBook() {
		return book;
	}
	public void setBook(int book) {
		this.book = book;
	}
	public int getActivity() {
		return activity;
	}
	public void setActivity(int activity) {
		this.activity = activity;
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
	public String getReject_unit_name() {
		return reject_unit_name;
	}
	public void setReject_unit_name(String reject_unit_name) {
		this.reject_unit_name = reject_unit_name;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public List<TechRejectItem> getItems() {
		return items;
	}
	public void setItems(List<TechRejectItem> items) {
		this.items = items;
	}
	public List<TechRejectPG> getPgroups() {
		return pgroups;
	}
	public void setPgroups(List<TechRejectPG> pgroups) {
		this.pgroups = pgroups;
	}
	public StaffActivity getActivityObj() {
		return activityObj;
	}
	public void setActivityObj(StaffActivity activityObj) {
		this.activityObj = activityObj;
	}
	public String getSa_type_name() {
		return sa_type_name;
	}
	public void setSa_type_name(String sa_type_name) {
		this.sa_type_name = sa_type_name;
	}
	public String getStaff_name() {
		return staff_name;
	}
	public void setStaff_name(String staff_name) {
		this.staff_name = staff_name;
	}
	public String getSa_remark() {
		return sa_remark;
	}
	public void setSa_remark(String sa_remark) {
		this.sa_remark = sa_remark;
	}
}
