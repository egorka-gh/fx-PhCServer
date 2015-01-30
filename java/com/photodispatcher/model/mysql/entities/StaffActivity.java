package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "staff_activity")
public class StaffActivity extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue()
    @Column(name="id")
    private int id;
    
    @Column(name="order_id")
    private String order_id;
    
    @Column(name="pg_id")
    private String pg_id;
    
    @Column(name="sa_group")
    private int sa_group;
    
    @Column(name="sa_type")
    private int sa_type;
    
    @Column(name="staff")
    private int staff;

    @Column(name="log_date")
    private Date log_date;

    @Column(name="remark")
    private String remark;
    
    //db drived
    @Column(name="sa_group_name", updatable=false, insertable=false)
    private String sa_group_name;
    @Column(name="sa_type_name", updatable=false, insertable=false)
    private String sa_type_name;
    @Column(name="staff_name", updatable=false, insertable=false)
    private String staff_name;
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
	public String getPg_id() {
		return pg_id;
	}
	public void setPg_id(String pg_id) {
		this.pg_id = pg_id;
	}
	public int getSa_group() {
		return sa_group;
	}
	public void setSa_group(int sa_group) {
		this.sa_group = sa_group;
	}
	public int getSa_type() {
		return sa_type;
	}
	public void setSa_type(int sa_type) {
		this.sa_type = sa_type;
	}
	public int getStaff() {
		return staff;
	}
	public void setStaff(int staff) {
		this.staff = staff;
	}
	public Date getLog_date() {
		return log_date;
	}
	public void setLog_date(Date log_date) {
		this.log_date = log_date;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSa_group_name() {
		return sa_group_name;
	}
	public void setSa_group_name(String sa_group_name) {
		this.sa_group_name = sa_group_name;
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
    
}
