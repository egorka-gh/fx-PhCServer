package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "rack_space")
public class RackSpace extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="rack")
    private int rack;
    @Column(name="name")
    private String name;

    @Column(name="package_source")
    private int source;
    @Column(name="package_id")
    private int packageID;
    @Column(name="box_id")
    private String boxID;

    
    @Column(name="width")
    private int width;
    @Column(name="height")
    private int height;
    @Column(name="weight")
    private float weight;

    //ref
    @Column(name="rack_type_name", updatable=false, insertable=false)
    private String rack_type_name;
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;
    @Column(name="source_name", updatable=false, insertable=false)
    private String sourceName;
    @Column(name="unused_weight", updatable=false, insertable=false)
    private double unused_weight;
    @Column(name="rating", updatable=false, insertable=false)
    private double rating;
    @Column(name="empty", updatable=false, insertable=false)
    private boolean empty;
    @Column(name="state", updatable=false, insertable=false)
    private int state;
    @Column(name="state_date", updatable=false, insertable=false)
    private Date state_date;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    @Column(name="box_num", updatable=false, insertable=false)
    private int box_num;
    
    @Transient
    private List<Order> orders;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public String getRack_name() {
		return rack_name;
	}
	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}
	public int getRack() {
		return rack;
	}
	public void setRack(int rack) {
		this.rack = rack;
	}
	public String getRack_type_name() {
		return rack_type_name;
	}
	public void setRack_type_name(String rack_type_name) {
		this.rack_type_name = rack_type_name;
	}
	public double getUnused_weight() {
		return unused_weight;
	}
	public void setUnused_weight(double unused_weight) {
		this.unused_weight = unused_weight;
	}

	/**
	 * ascending decision num
	 * lesser - better
	 * -1 - order space or package own space
	 * @return
	 */
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public List<Order> getOrders() {
		return orders;
	}
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	public boolean isEmpty() {
		return empty;
	}
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getPackageID() {
		return packageID;
	}
	public void setPackageID(int packageID) {
		this.packageID = packageID;
	}
	public String getBoxID() {
		return boxID;
	}
	public void setBoxID(String boxID) {
		this.boxID = boxID;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
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
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public int getBox_num() {
		return box_num;
	}
	public void setBox_num(int box_num) {
		this.box_num = box_num;
	}

}
