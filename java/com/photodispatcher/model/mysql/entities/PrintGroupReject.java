package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "print_group_rejects")
public class PrintGroupReject extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="print_group")
    private String print_group;
    @Column(name="thech_unit")
    private int thech_unit;
    @Column(name="book")
    private int book;
    @Column(name="sheet")
    private int sheet;
    @Column(name="activity")
    private int activity;

    //db drived
    @Column(name="thech_unit_name", updatable=false, insertable=false)
    private String thech_unit_name;
    @Column(name="sa_type_name", updatable=false, insertable=false)
    private String sa_type_name;
    @Column(name="staff_name", updatable=false, insertable=false)
    private String staff_name;
    @Column(name="sa_remark", updatable=false, insertable=false)
    private String sa_remark;
    @Column(name="state", updatable=false, insertable=false)
    private int state;
    @Column(name="state_date", insertable=false, updatable=false)
    private Date state_date;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPrint_group() {
		return print_group;
	}
	public void setPrint_group(String print_group) {
		this.print_group = print_group;
	}
	public int getThech_unit() {
		return thech_unit;
	}
	public void setThech_unit(int thech_unit) {
		this.thech_unit = thech_unit;
	}
	public int getBook() {
		return book;
	}
	public void setBook(int book) {
		this.book = book;
	}
	public int getSheet() {
		return sheet;
	}
	public void setSheet(int sheet) {
		this.sheet = sheet;
	}
	public int getActivity() {
		return activity;
	}
	public void setActivity(int activity) {
		this.activity = activity;
	}
	public String getThech_unit_name() {
		return thech_unit_name;
	}
	public void setThech_unit_name(String thech_unit_name) {
		this.thech_unit_name = thech_unit_name;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
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

}
