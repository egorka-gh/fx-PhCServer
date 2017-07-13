package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "order_books")
public class OrderBooks extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="pg_id")
    private String pg_id;
    @Id
    @Column(name="book")
    private int book;
    @Column(name="target_pg")
    private String target_pg;
    @Column(name="sheets")
    private int sheets;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="is_rejected")
    private boolean is_rejected;
    @Column(name="is_reject")
    private boolean is_reject;

	//ref
    @Column(name="state_name", insertable=false, updatable=false)
    private String state_name;
    @Column(name="book_part_name", insertable=false, updatable=false)
    private String book_part_name;
    @Column(name="sa_type_name", updatable=false, insertable=false)
    private String sa_type_name;
    @Column(name="staff_name", updatable=false, insertable=false)
    private String staff_name;
    @Column(name="sa_remark", updatable=false, insertable=false)
    private String sa_remark;
	public String getPg_id() {
		return pg_id;
	}
	public void setPg_id(String pg_id) {
		this.pg_id = pg_id;
	}
	public int getBook() {
		return book;
	}
	public void setBook(int book) {
		this.book = book;
	}
	public String getTarget_pg() {
		return target_pg;
	}
	public void setTarget_pg(String target_pg) {
		this.target_pg = target_pg;
	}
	public int getSheets() {
		return sheets;
	}
	public void setSheets(int sheets) {
		this.sheets = sheets;
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
	public boolean isIs_rejected() {
		return is_rejected;
	}
	public void setIs_rejected(boolean is_rejected) {
		this.is_rejected = is_rejected;
	}
	public boolean isIs_reject() {
		return is_reject;
	}
	public void setIs_reject(boolean is_reject) {
		this.is_reject = is_reject;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public String getBook_part_name() {
		return book_part_name;
	}
	public void setBook_part_name(String book_part_name) {
		this.book_part_name = book_part_name;
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
