package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "package_box")
public class MailPackageBox extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="package_id")
    private int packageID;
    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="box_id")
    private String boxID;
    @Column(name="box_num")
    private int boxNum;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="barcode")
    private String barcode;
    @Column(name="price")
    private float price;
    @Column(name="weight")
    private int weight;
    
    //ref
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    
    //childs
    @Transient
    private List<MailPackageBoxItem> items;
    @Transient
    private List<OrderBook> books;

	public int getPackageID() {
		return packageID;
	}

	public void setPackageID(int packageID) {
		this.packageID = packageID;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public String getBoxID() {
		return boxID;
	}

	public void setBoxID(String boxID) {
		this.boxID = boxID;
	}

	public int getBoxNum() {
		return boxNum;
	}

	public void setBoxNum(int boxNum) {
		this.boxNum = boxNum;
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

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getState_name() {
		return state_name;
	}

	public void setState_name(String state_name) {
		this.state_name = state_name;
	}

	public List<MailPackageBoxItem> getItems() {
		return items;
	}

	public void setItems(List<MailPackageBoxItem> items) {
		this.items = items;
	}

	public List<OrderBook> getBooks() {
		return books;
	}

	public void setBooks(List<OrderBook> books) {
		this.books = books;
	}

}
