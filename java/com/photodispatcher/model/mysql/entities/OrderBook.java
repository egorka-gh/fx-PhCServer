package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "order_books")
public class OrderBook extends AbstractEntity {
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
    @Column(name="compo_type")
    private int compo_type;
    @Column(name="compo_pg")
    private String compo_pg;
    @Column(name="compo_book")
    private int compo_book;

	//ref
    @Column(name="order_id", insertable=false, updatable=false)
    private String order_id;
    @Column(name="sub_id", insertable=false, updatable=false)
    private String sub_id;
    @Column(name="state_name", insertable=false, updatable=false)
    private String state_name;
    @Column(name="book_part_name", insertable=false, updatable=false)
    private String book_part_name;
    @Column(name="book_part", insertable=false, updatable=false)
    private int book_part;
    @Column(name="compo_type_name", insertable=false, updatable=false)
    private String compo_type_name;
    
    /*
    @Column(name="state2", insertable=false, updatable=false)
    private int state2;
    @Column(name="state_date2", insertable=false, updatable=false)
    private Date state_date2;
    @Column(name="state_name2", insertable=false, updatable=false)
    private String state_name2;
    */

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
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getBook_part() {
		return book_part;
	}
	public void setBook_part(int book_part) {
		this.book_part = book_part;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public int getCompo_type() {
		return compo_type;
	}
	public void setCompo_type(int compo_type) {
		this.compo_type = compo_type;
	}
	public String getCompo_pg() {
		return compo_pg;
	}
	public void setCompo_pg(String compo_pg) {
		this.compo_pg = compo_pg;
	}
	public int getCompo_book() {
		return compo_book;
	}
	public void setCompo_book(int compo_book) {
		this.compo_book = compo_book;
	}
	public String getCompo_type_name() {
		return compo_type_name;
	}
	public void setCompo_type_name(String compo_type_name) {
		this.compo_type_name = compo_type_name;
	}
	
	/*
	public int getState2() {
		return state2;
	}
	public void setState2(int state2) {
		this.state2 = state2;
	}
	public Date getState_date2() {
		return state_date2;
	}
	public void setState_date2(Date state_date2) {
		this.state_date2 = state_date2;
	}
	public String getState_name2() {
		return state_name2;
	}
	public void setState_name2(String state_name2) {
		this.state_name2 = state_name2;
	}
	*/
    
}
