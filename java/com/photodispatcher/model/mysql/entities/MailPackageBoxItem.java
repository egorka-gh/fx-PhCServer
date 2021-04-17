package com.photodispatcher.model.mysql.entities;


import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "package_box_item")
public class MailPackageBoxItem extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="box_id")
    private String boxID;
    @Id
    @Column(name="order_id")
    private String orderID;
    @Id
    @Column(name="alias")
    private String alias;
    @Column(name="item_from")
    private int itemFrom;
    @Column(name="item_to")
    private int itemTo;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    @Column(name="book_type", updatable=false, insertable=false)
    private int bookType;
    @Column(name="book_type_name", updatable=false, insertable=false)
    private String bookTypeName;

    //child 
    @Transient
    private List<PrintGroup> printGroups;

    
	public String getBoxID() {
		return boxID;
	}
	public void setBoxID(String boxID) {
		this.boxID = boxID;
	}
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public int getItemFrom() {
		return itemFrom;
	}
	public void setItemFrom(int itemFrom) {
		this.itemFrom = itemFrom;
	}
	public int getItemTo() {
		return itemTo;
	}
	public void setItemTo(int itemTo) {
		this.itemTo = itemTo;
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
	public List<PrintGroup> getPrintGroups() {
		return printGroups;
	}
	public void setPrintGroups(List<PrintGroup> printGroups) {
		this.printGroups = printGroups;
	}
	public String getBookTypeName() {
		return bookTypeName;
	}
	public void setBookTypeName(String bookTypeName) {
		this.bookTypeName = bookTypeName;
	}
	public int getBookType() {
		return bookType;
	}
	public void setBookType(int bookType) {
		this.bookType = bookType;
	}
    public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}

    
}
