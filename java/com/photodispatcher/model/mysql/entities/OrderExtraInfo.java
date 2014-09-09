package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "order_extra_info")
public class OrderExtraInfo extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;
    @Id
    @Column(name="sub_id")
    private String sub_id;
    
    @Column(name="endpaper")
    private String endpaper;
    @Column(name="interlayer")
    private String interlayer;
    @Column(name="calc_type")
    private String calc_type;
    @Column(name="cover")
    private String cover;
    @Column(name="format")
    private String format;
    @Column(name="corner_type")
    private String corner_type;
    @Column(name="kaptal")
    private String kaptal;
    
    //ref
    @Column(name="book_type", updatable=false, insertable=false)
    private int book_type;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEndpaper() {
		return endpaper;
	}
	public void setEndpaper(String endpaper) {
		this.endpaper = endpaper;
	}
	public String getInterlayer() {
		return interlayer;
	}
	public void setInterlayer(String interlayer) {
		this.interlayer = interlayer;
	}
	public String getCalc_type() {
		return calc_type;
	}
	public void setCalc_type(String calc_type) {
		this.calc_type = calc_type;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getCorner_type() {
		return corner_type;
	}
	public void setCorner_type(String corner_type) {
		this.corner_type = corner_type;
	}
	public String getKaptal() {
		return kaptal;
	}
	public void setKaptal(String kaptal) {
		this.kaptal = kaptal;
	}
	public String getSub_id() {
		return sub_id;
	}
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	public int getBook_type() {
		return book_type;
	}
	public void setBook_type(int book_type) {
		this.book_type = book_type;
	}
    
}
