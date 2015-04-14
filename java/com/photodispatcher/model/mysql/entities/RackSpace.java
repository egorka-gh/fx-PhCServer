package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name="width")
    private int width;
    @Column(name="height")
    private int height;
    @Column(name="weight")
    private float weight;
    /*
    @Column(name="package_source")
    private int package_source;
    @Column(name="package_id")
    private int package_id;
    */

    //ref
    @Column(name="rack_type_name", updatable=false, insertable=false)
    private String rack_type_name;
    @Column(name="rack_name", updatable=false, insertable=false)
    private String rack_name;
    @Column(name="unused_weight", updatable=false, insertable=false)
    private double unused_weight;
    @Column(name="rating", updatable=false, insertable=false)
    private double rating;
    
    /*
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    */
    
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
	/*
	public int getPackage_source() {
		return package_source;
	}
	public void setPackage_source(int package_source) {
		this.package_source = package_source;
	}
	public int getPackage_id() {
		return package_id;
	}
	public void setPackage_id(int package_id) {
		this.package_id = package_id;
	}
	*/
	public String getRack_name() {
		return rack_name;
	}
	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}
	/*
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	*/
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
    

}
